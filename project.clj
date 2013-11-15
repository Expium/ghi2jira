(defproject ghijira/ghijira "1.2" 
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [tentacles "0.2.6"]
                 [org.clojure/data.csv "0.1.2"]
                 [clj-time "0.6.0"]
                 [org.clojure/core.memoize "0.5.6"]]
  :main ghijira.core
  :min-lein-version "2.0.0"
  :description "Export GitHub Issues to JIRA-compatible CSV format")
