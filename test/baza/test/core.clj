; test/baza/core.clj: tests for src/baza/core.clj. 
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

(ns baza.test.core
  (:use [baza.core] :reload)
  (:use [clojure.test]))

(deftest test-parse-date
  (testing "test if dates are parsed correctly."
    (is (= (parse-date "woe 1 januari 2011") {:day 1 :month 1 :year 2011}))
    (is (= (parse-date "foo 60 februari 2011") {:day 60 :month 2 :year 2011}))
    (is (= (parse-date "di 8 maart 2011") {:day 8 :month 3 :year 2011}))
    (is (= (parse-date "vrijdag 30 april 2011") {:day 30 :month 4 :year 2011}))
    (is (= (parse-date "ma 4 mei 2011") {:day 4 :month 5 :year 2011}))
    (is (= (parse-date "ma 6 juni 2011") {:day 6 :month 6 :year 2011}))
    (is (= (parse-date "ma 14 juli 2011") {:day 14 :month 7 :year 2011}))
    (is (= (parse-date "zo 4 augustus 2011") {:day 4 :month 8 :year 2011}))
    (is (= (parse-date "di 3 september 2011") {:day 3 :month 9 :year 2011}))
    (is (= (parse-date "do 9 oktober 2011") {:day 9 :month 10 :year 2011}))
    (is (= (parse-date "za 4 november 2011") {:day 4 :month 11 :year 2011}))
    (is (= (parse-date "ma 25 december 2011") {:day 25 :month 12 :year 2011}))

    (is (thrown-with-msg? Exception
          #"requires a non-empty string input date argument"
          (parse-date "")))))

(deftest test-create-date-string
  (testing "test if date strings are formatted correctly."
    (is (=
          (date-string {:day 1 :month 5 :year 2011} "09:00:00")
          "2011-05-01T09:00:00+02:00"))
    (is (=
          (date-string {:day 31 :month 5 :year 2011} "09:00:00")
          "2011-05-31T09:00:00+02:00"))))

(deftest test-create-people-html
  (testing "test if HTML code for a people list is created correctly."
    (let [people [{:status "p", :name "Berlijn, Jorie"}
                  {:status "v", :name "Kunst, Joke"}
                  {:status "", :name "Busser, Bep"}
                  {:status "", :name "Hercules, Ria van"}
                  {:status "", :name "IJsselstein, Dani?lle"}
                  {:status "r", :name "Kuilman, Anneke"}
                  {:status "", :name "Pels Rijcken, Miek"}
                  {:status "", :name "Schagen, Atie van"}
                  {:status "", :name "Walraven, Meta"}]
          people-html (str
                        "<ul><li>Berlijn, Jorie (primus)</li>"
                        "<li>Kunst, Joke (vroege vogel)</li>"
                        "<li>Busser, Bep</li>"
                        "<li>Hercules, Ria van</li>"
                        "<li>IJsselstein, Dani?lle</li>"
                        "<li>Kuilman, Anneke (rondleider)</li>"
                        "<li>Pels Rijcken, Miek</li>"
                        "<li>Schagen, Atie van</li>"
                        "<li>Walraven, Meta</li></ul>")]
      (is (= (create-people-html people) people-html)))))

(deftest test-two-digit-format
  (testing "test if integers are converted to two digit strings"
    (is (= (two-digit-format 1) "01"))
    (is (= (two-digit-format 10) "10"))
    (is (= (two-digit-format 30) "30"))
    (is (= (two-digit-format "1") "01"))
    (is (= (two-digit-format "10") "10"))
    (is (= (two-digit-format "30") "30"))))

(deftest test-create-entry
  (testing "Test if entries are created correctly."
    ; test DST
    (let [entry (create-entry {:name "F.M. de Waard" :email "fmw@vix.io"}
                              {:day 4 :month 8 :year 2011}
                              "09:00:00"
                              "12:00:00"
                              "This years birthday"
                              "<h1>So awesome!</h1>")]

      (is (= (.getText (.getTitle entry)) "This years birthday"))
      (is (= (.getHtml (.getContent (.getContent entry)))
             "<h1>So awesome!</h1>"))
      
      (is (= (.getName (first (.getAuthors entry)))
             "F.M. de Waard"))

      (is (= (.getEmail (first (.getAuthors entry)))
             "fmw@vix.io"))

      (is (= (str (.getStartTime (first (.getTimes entry))))
             "2011-08-04T09:00:00.000+02:00"))

      (is (= (str (.getEndTime (first (.getTimes entry))))
             "2011-08-04T12:00:00.000+02:00")))

    ; test non-DST
    (let [entry (create-entry {:name "F.M. de Waard" :email "fmw@vix.io"}
                              {:day 1 :month 1 :year 2011}
                              "09:00:00"
                              "12:00:00"
                              "This years birthday"
                              "<h1>So awesome!</h1>")]

      (is (= (.getText (.getTitle entry)) "This years birthday"))
      (is (= (.getHtml (.getContent (.getContent entry)))
             "<h1>So awesome!</h1>"))
      
      (is (= (.getName (first (.getAuthors entry)))
             "F.M. de Waard"))

      (is (= (.getEmail (first (.getAuthors entry)))
             "fmw@vix.io"))

      (is (= (str (.getStartTime (first (.getTimes entry))))
             "2011-01-01T09:00:00.000+01:00"))

      (is (= (str (.getEndTime (first (.getTimes entry))))
             "2011-01-01T12:00:00.000+01:00")))))

