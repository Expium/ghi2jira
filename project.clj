(defproject ghijira/ghijira "1.1" 
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [tentacles "0.2.3"]
                 [org.clojure/data.csv "0.1.2"]
                 [clj-time "0.5.0"]
                 [org.clojure/core.memoize "0.5.3"]]
  :main ghijira.core
  :min-lein-version "2.0.0"
  :plugins [[lein-eclipse "1.0.0"]]
  :description "Export GitHub Issues to JIRA-compatible CSV format")
