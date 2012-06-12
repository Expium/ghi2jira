(defproject ghijira "1.0.0-SNAPSHOT"
  :description "Export GitHub Issues to JIRA-compatible CSV format"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [tentacles "0.2.0-beta1"]
                 [org.clojure/data.csv "0.1.2"]
                 [clj-time "0.4.2"]]

  :dev-dependencies [
        [lein-eclipse "1.0.0"]
  ]
)
