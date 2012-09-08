; Export GitHub Issues to JIRA-compatible CSV format

; Copyright 2012 Kyle Cordes
; http://kylecordes.com/

; Hereby released under the Eclipse Public License 1.0,
; the same license as Clojure uses.

(ns ghijira.core
  "Export GitHub Issues to JIRA-compatible CSV format"
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
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

;; Loading issues from GH

(defn get-issues [state]
  (issues/issues ghuser ghproject {:auth auth :all-pages true :state state}))

(defn get-open-issues [] (get-issues "open"))

(defn get-closed-issues [] (get-issues "closed"))

(defn get-all-issues [] (concat (get-open-issues) (get-closed-issues)))

(defn comments-for [issue]
  (issues/issue-comments ghuser ghproject (:number issue) {:auth auth}))

(defn assoc-comments [issue]
  (assoc issue :comment-contents (comments-for issue)))

(defn add-comments [issues]
  (map assoc-comments issues))

;; Validation / preprocessing

(defn find-missing-issues [issues]
  (let [numbers (set (map :number issues))
        max-number (apply max numbers)
        expected (range 1 (inc max-number))]
    (remove numbers expected)))

(defn warn-missing-issues [issues]
  (let [missing-issues (find-missing-issues issues)]
    (when-not (empty? missing-issues)
      (println)
      (println "WARNING: Some issues are missing from the set. This will result in inconsistent numeration between JIRA and Github.")
      (println)
      (println "The missing issues are:" (str/join ", " missing-issues))
      (println))))

;; Export to JIRA

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

(defn gh2jira [date]
  (tf/unparse jira-formatter (tf/parse gh-formatter date)))

(defn get-user [issue]
  (let [u (:login (:user issue))]
    (get user-map u u)))

(defn format-comment [c]
  (let [created-at (tf/parse gh-formatter (:created_at c))]
    (str "Comment:"
         (get-user c)
         ":"
         (tf/unparse jira-formatter created-at)
         ":" \newline \newline
         ; JIRA thinks # means a comment in the CSV file, so drop those.
         (str/replace (:body c) \# \_ )
         )))

(defn issue2row [issue]
  (let [comments (take MAXCMT (:comment-contents issue))]
    (concat
      (vector
        (:number issue) 
        (:title issue)
        (:body issue)
        (gh2jira (:created_at issue))
        (gh2jira (:updated_at issue))
        "Task" ; issue type
        (:title (:milestone issue))
        (if (= "closed" (:state issue)) "Closed" "Open")
        (get-user issue) )
      (map format-comment comments)
      (repeat (- MAXCMT (count comments)) "")    ; pad out field count  
    )))

(defn export-issues-to-file [issues filename]
  (let [issues-in-order (sort-by :number issues)]
    (with-open [out-file (io/writer filename)]
      (csv/write-csv 
        out-file 
        (concat
          [columns] 
          (map issue2row issues-in-order))))))

;; Main

(defn -main [& args]
  (let [issues (add-comments (get-all-issues))]
    (warn-missing-issues issues)
    (export-issues-to-file issues "JIRA.csv")))