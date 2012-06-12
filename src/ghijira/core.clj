; Export GitHub Issues to JIRA-compatible CSV format

; Copyright 2012 Kyle Cordes
; http://kylecordes.com/

; Hereby released under the Eclipse Public License 1.0,
; the same license as Clojure uses.

(ns ghijira.core
  "Export GitHub Issues to JIRA-compatible CSV format"
  (:require [clojure.pprint :as pp]
            [clojure.string :only replace]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [tentacles.issues :as issues]
            [clj-time.core :as time]
            [clj-time.format :as tf] ))

(load-file "config.clj")

; Use the all-pages mechanism in tentacles to retrieve a complete list
; of issues. I found that several of the GH export scripts on the web
; ignore everything past the first page, making them useless for all
; but very small projects.

; To test quickly with a smaller number of issues, change :all-pages true to
; :per-page 20 

(def iss-open (issues/issues ghuser ghproject {:auth auth :all-pages true :state "open"}))
(def iss-closed (issues/issues ghuser ghproject {:auth auth :all-pages true :state "closed"}))
(def issues (concat iss-open iss-closed))

(defn comments-for [issue]
  (println (:number issue))
  (issues/issue-comments ghuser ghproject (:number issue) {:auth auth}))

; Load all the comments
(def cmtmap (zipmap (map :number issues) (map comments-for issues)))

(def columns 
  (concat
    ["Issue Id",
     "Summary",
     "Description",
     "Date Created",
     "Date Modified",
     "Issue type",
     "Milestone",
     "Status",
     "Reporter"]
    (repeat MAXCMT "Comments") ))

; Date-time format used by the Github Issues API
(def gh-formatter (tf/formatters :date-time-no-ms))

; For most date fields, JIRA can handle anything in SimpleDateFormat
; can be anything for SimpleDateFormat. For comment dates, JIRA requires
; this specific format only. 
(def jira-formatter (tf/formatter "MM/dd/yy hh:mm:ss a"))

(defn get-user [issue]
  (let [u (:login (:user issue))]
    (get user-map u u)))

(defn format-comment [c]
  (let [created-at (tf/parse gh-formatter (:created_at c))
        ]
    (str "Comment:"
         (get-user c)
         ":"
         (tf/unparse jira-formatter created-at)
         ":" \newline \newline
         ; JIRA thinks # means a comment in the CSV file, so drop those.
         (clojure.string/replace (:body c) \# \_ )
         )))

(defn issue2row [issue]
  (let [created-at (tf/parse gh-formatter (:created_at issue))
        updated_at (tf/parse gh-formatter (:updated_at issue))
        comments (take MAXCMT (cmtmap (:number issue)))]
    (concat
      (vector
        (:number issue) 
        (:title issue)
        (:body issue)
        (tf/unparse jira-formatter created-at)
        (tf/unparse jira-formatter updated_at)
        "Task" ; issue type
        (:title (:milestone issue))
        (if (= "closed" (:state issue)) "Closed" "Open")
        (get-user issue) )
      (map format-comment comments)
      (repeat (- MAXCMT (count comments)) "")    ; pad out field count  
    )))

(with-open [out-file (io/writer "JIRA.csv")]
  (csv/write-csv out-file (concat [columns] (map issue2row issues))))
