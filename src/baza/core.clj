; src/baza/core.clj: posts scraped data from Museumplan.nl to Google Calendar.
;
; Copyright 2011, F.M. (Filip) de Waard <fmw@vix.io>.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns baza.core
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use [clojure.string :only (split blank?)]
        [clj-time.core :only (date-time from-time-zone time-zone-for-offset)]
        [hiccup.core :only (html)])
  (:require [net.cgrand.enlive-html :as html]
            [clj-http.client :as http-client]
            [clojure.contrib [error-kit :as kit]])
  (:import (java.net URL)
           (com.google.gdata.client.calendar CalendarService CalendarQuery)
           (com.google.gdata.data
             Feed Person DateTime PlainTextConstruct HtmlTextConstruct)
           (com.google.gdata.data.extensions EventEntry When)))

(kit/deferror EmptyDateInput [] []
  {:msg "(parse-date) requires a non-empty string input date argument"
   :unhandled (kit/throw-msg Exception)})

(defn process-morning [row]
  {:status (html/text (first (html/select row
      [[:td (html/nth-of-type 2)] [:center] [:font]])))
   :name (html/text (first (html/select row
      [[:td (html/nth-of-type 3)] [:i] [:a]])))})

(defn process-afternoon [row]
  {:status (html/text (first (html/select row
      [[:td (html/nth-of-type 4)] [:center] [:font]])))
   :name (html/text (first (html/select row
      [[:td (html/nth-of-type 5)] [:i] [:a]])))})

(defn process-day [rows]
    {:date
      (html/text (first (html/select (first rows)
      [[:td (html/nth-of-type 1)] [:b]])))
     :morning (map process-morning rows)
     :afternoon (map process-afternoon rows)})

(defn process-week [data]
  (let [rows (html/select (html/html-resource (java.io.StringReader. data))
                          [[:div.standardborder] [:tr]])
        num-partners-per-day (/ (dec (count rows)) 6)]
    (map process-day (partition num-partners-per-day (rest rows)))))

(defn get-weeks [data]
  (map #(:value (:attrs %))
       (html/select (html/html-resource (java.io.StringReader. data))
           [[:option]])))

(defn request-week [week-id uri username password]
    (process-week (:body (http-client/post
        uri {:basic-auth [username password]
             :content-type "application/x-www-form-urlencoded"
             :body (str "edit_week=" week-id)}))))

(defn process! [uri username password]
    (map #(request-week % uri username password) (get-weeks
        (:body (http-client/get uri {:basic-auth [username password]})))))

(defn parse-date [date]
  (if-not (blank? date)
    (let [date-words (split date #" ")
          month-full (nth date-words 2)]
      {:day (Integer/parseInt (nth date-words 1))
       :year (Integer/parseInt (nth date-words 3))
       :month (cond
               (= month-full "januari") 1
               (= month-full "februari") 2
               (= month-full "maart") 3
               (= month-full "april") 4
               (= month-full "mei") 5
               (= month-full "juni") 6
               (= month-full "juli") 7
               (= month-full "augustus") 8
               (= month-full "september") 9
               (= month-full "oktober") 10
               (= month-full "november") 11
               (= month-full "december") 12)})
  (kit/raise EmptyDateInput)))

(defn create-people-html [people-raw]
  (let [people (map #(if (blank? (:status %))
                       (:name %)
                       (str
                          (:name %)
                          " ("
                         (cond
                           (= (:status %) "v")
                            "vroege vogel"
                           (= (:status %) "p")
                            "primus"
                           (= (:status %) "r")
                            "rondleider")
                          ")"))
                    people-raw)]
    (html [:ul (for [x (range 0 (count people))] [:li (nth people x)])])))

(defn two-digit-format [n]
  (let [d (if (string? n)
            (Integer/parseInt n)
            n)]
    (if (>= d 10)
      (str d)
      (str "0" d))))

(defn date-string [date time-string]
  (let [offset-milli (.getOffset
                       (java.util.TimeZone/getTimeZone "Europe/Amsterdam")
                       1 (:year date) (- (:month date) 1) (:day date) 1 1)
        offset (cond
                 (= offset-milli 3600000)
                   "+01:00"
                 (= offset-milli 7200000)
                   "+02:00")]
    (str
      (:year date)
      "-"
      (two-digit-format (:month date))
      "-"
      (two-digit-format (:day date))
      "T"
      time-string
      offset)))

(defn create-entry [author date from-time to-time title html-content]
  (let [author (Person. (:name author) nil (:email author))
        entry (EventEntry.)
        entry-when (When.)]

    (.add (.getAuthors entry) author)

    (doto entry-when
      (.setStartTime (DateTime/parseDateTime (date-string date from-time)))
      (.setEndTime (DateTime/parseDateTime (date-string date to-time))))

    (doto entry
      (.setTitle (PlainTextConstruct. title))
      (.setContent (HtmlTextConstruct. html-content))
      (.addTime entry-when))

    entry))

(defn create-day-planning [day-map]
  (let [morning-entry (create-entry {:name "F.M. de Waard" :email "fmw@vix.io"}
                                    (parse-date (:date day-map))
                                    "09:00:00"
                                    "12:00:00"
                                    "BaZ rooster ochtend"
                                    (create-people-html (:morning day-map)))
        afternoon-entry (create-entry {:name "F.M. de Waard"
                                       :email "fmw@vix.io"}
                                    (parse-date (:date day-map))
                                    "12:00:00"
                                    "17:00:00"
                                    "BaZ rooster middag"
                                    (create-people-html (:afternoon day-map)))]


    {:morning morning-entry
     :afternoon afternoon-entry}))

(defn clear-entries-for-day! [post-uri service date]
  (let [query (CalendarQuery. post-uri)]

    (doto query
      (.setFullTextQuery "BaZ rooster")
      (.setMinimumStartTime
        (DateTime/parseDateTime (date-string date "00:00:00")))
      (.setMaximumStartTime
        (DateTime/parseDateTime (date-string date "23:59:59"))))

    (let [results-feed (.query service query (class (Feed.)))
          results (.getEntries results-feed)]

      (doseq [r results]
        (.setService r service)
        (.delete r)))))

(defn clear-entries! [post-uri service]
  (let [query (CalendarQuery. post-uri)]

    (.setFullTextQuery query "BaZ rooster")

    (let [results-feed (.query service query (class (Feed.)))
          results (.getEntries results-feed)]

      (doseq [r results]
        (.setService r service)
        (.delete r))

      (when-not (empty? results)
        (recur post-uri service)))))

(defn post-day-planning! [post-uri service day-map]
  (let [day-planning (create-day-planning day-map)]

    (clear-entries-for-day! post-uri service (parse-date (:date day-map)))

    {:morning (.insert service post-uri (:morning day-planning))
     :afternoon (.insert service post-uri (:afternoon day-planning))}))

(defn post! [uri data username password]
  (let [service (CalendarService. "NetCollective-baza-0.1")
        post-uri (URL. uri)]

    (.setUserCredentials service username password)

    (doall (map #(doall (map (fn [day-data]
                 (post-day-planning! post-uri service day-data)) %)) data))))

(defn -main [& args]
  (with-command-line args
    "Command line demo"
      [[google-username "Google Calendar username"]
       [google-password "Google Calendar password"]
       [calendar-uri "Google Calendar URI"]
       [mp-uri "MuseumPlan.nl Calendar URI"]
       [mp-username "Museumplan.nl username"]
       [mp-password "Museumplan.nl password"]
       remaining]
    (when-not (empty? args)
      (let [data (process! mp-uri mp-username mp-password)]
        (do
          (let [events
                (post! calendar-uri data google-username google-password)]
            (println (str
                       "Successfully posted events to Google Calendar."))))))))
