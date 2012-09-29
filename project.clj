(defproject ghijira/ghijira "1.0.0-SNAPSHOT" 
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [tentacles "0.2.0-beta1"]
                 [org.clojure/data.csv "0.1.2"]
                 [clj-time "0.4.2"]]
  :main ghijira.core
  :min-lein-version "2.0.0"
  :plugins [[lein-eclipse "1.0.0"]]
  :description "Export GitHub Issues to JIRA-compatible CSV format")
