; project.clj: Leiningen project file for Baza application.
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

(defproject baza "1.0.0-SNAPSHOT"
  :description "Posts Museum Beelden aan Zee planning to Google Calendar."
  :license {:name "Apache License, version 2."}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [clj-http "0.1.3"]
                 [clj-time "0.3.0"]
                 [hiccup "0.3.4"]
                 [com.google.gdata/gdata-client-1.0 "1.41.5"]
                 [com.google.gdata/gdata-calendar-2.0 "1.41.5"]]
  :dev-dependencies [[robert/hooke "1.1.0"]
                     [org.clojars.autre/lein-vimclojure "1.0.0"]]
  :repositories {"mandubian-mvn" "http://mandubian-mvn.googlecode.com/svn/trunk/mandubian-mvn/repository"}
  :test-selectors {:default (fn [v] (not (:integration v)))
                   :integration :integration
                   :all (fn [_] true)}
  :main baza.core)
