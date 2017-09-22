; Export GitHub Issues to JIRA-compatible CSV format

; Copyright 2012-2013 Kyle Cordes
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
            [clj-time.format :as tf]
            [clojure.core.memoize :as memoize] ))

(def UNNAMED "UNNAMED")

(def ^:dynamic *config-id*)
(def ^:dynamic *ghuser*)
(def ^:dynamic *ghproject*)
(def ^:dynamic *auth*)
(def ^:dynamic *maxcmt*)
(def ^:dynamic *user-map*)
(def ^:dynamic *ignore-mentions*)
(def ^:dynamic *jira-project*)
(def ^:dynamic *git-base-url*)
(def ^:dynamic *issue-offset*)

; Use the all-pages mechanism in tentacles to retrieve a complete list
; of issues. I found that several of the GH export scripts on the web
; ignore everything past the first page, making them useless for all
; but very small projects.

; To test quickly with a smaller number of issues, change :all-pages true to
; :per-page 20

;; Loading issues from GH

(defn get-issues [state]
  (let [x (issues/issues *ghuser* *ghproject* {:auth *auth* :all-pages true :state state})]
    (if (= (:status x) 404)
      (throw (Exception. "Error from GitHub. API returned 404, repo owner, name, or auth is wrong")))
    x))

(defn get-all-issues []
  (concat (get-issues "open")
          (get-issues "closed")))

(defn assoc-comments-and-events [issue]
  (println (str "Fetching data for #" (:number issue)))
  (assoc issue
    :comment-contents (issues/issue-comments *ghuser* *ghproject* (:number issue) {:auth *auth*})
    :event-contents (issues/issue-events *ghuser* *ghproject* (:number issue) {:auth *auth*})))

(defn issues-with-extra []
  (let [iss (get-all-issues)]
    (println "Issues found: " (count iss))
    (if (> (count iss) 0)
      (map assoc-comments-and-events iss)
      (throw (Exception. "No issues found, this usually means the repo owner or name is wrong.")))))

; Cache for 15 minutes, for easier development at the REPL
(def issues-with-extra-cached
  (memoize/ttl issues-with-extra :ttl/threshold (* 15 60 1000)))

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
      (println "WARNING: Some issues are missing from the set. This will result in inconsistent numbering between JIRA and Github.")
      (println "This could be fixed by adding a feature to insert 'stubs' items.")
      (println)
      (println "The missing issues are:" (str/join ", " missing-issues))
      (println))))

;; Export to JIRA

(defn columns []
  (concat
    ["Issue Id",
     "Summary",
     "Description",
     "Date Created",
     "Date Modified",
     "Issue type",
     "Milestone",
     "Status",
     "Resolution",
     "Reporter",
     "Assignee",
     "Labels"]
    (repeat *maxcmt* "Comments") ))

; Date-time format used by the Github Issues API
(def gh-formatter (tf/formatters :date-time-no-ms))

; For most date fields, JIRA can handle anything in SimpleDateFormat
; can be anything for SimpleDateFormat. For comment dates, JIRA requires
; this specific format only.
(def jira-formatter (tf/formatter "MM/dd/yy hh:mm:ss a"))

(defn gh2jira [date]
  (tf/unparse jira-formatter (tf/parse gh-formatter date)))

(defn get-user [issue]
  (let [u (or (:login (:user issue))
              (:login (:actor issue)))]
    (get *user-map* u u)))

(defn bad-mention? [comment]
  (if (get *ignore-mentions* (get-user comment))
    true
    false))

(defn get-comment-author [comment]
  (let [u (get-user comment)]
    (if-not (get *ignore-mentions* u)
      (str u ";")
      "")))

(defn get-assignee [issue]
  (let [u (:login (:assignee issue))]
    (get *user-map* u u)))

(defn cross-item-ref-replace
  ""
  [comment project issue-offset]
  (-> comment
    (str/replace #"#(\d+)\b"
                 #(str project "-" (+ issue-offset (bigdec (second %1)))))
    (str/replace \#
                 \_ ))) ; Drop #, JIRA does not like.

;(cross-item-ref-replace "Test that mentions #14 here" "ABC" 150)

(defn comment-or-event-to-text
  [c]
  (cond
    (= (:event c) "referenced") (str "Referenced in commit:\n"
                                     *git-base-url*
                                     (:commit_id c)
                                     "\n")
    (= (:event c) "merged")       (str "Merged this pull request")
    (= (:event c) "labeled")      (str "Added the '"   (:name (:label c)) "' label")
    (= (:event c) "unlabeled")    (str "Removed the '" (:name (:label c)) "' label")
    (= (:event c) "milestoned")   (str "Added the '"   (:title (:milestone c)) "' milestone")
    (= (:event c) "demilestoned") (str "Removed the '" (:title (:milestone c)) "' milestone")
    (= (:event c) "renamed")      (str "Renamed this issue from '" (:from (:rename c)) "' to '" (:to (:rename c)) "'")
    (:event c) (str (:event c))
    :else  (cross-item-ref-replace
            (:body c) *jira-project* *issue-offset*)))

(defn format-comment [c]
  (let [created-at (tf/parse gh-formatter (:created_at c))
        comment-text (comment-or-event-to-text c)]
    (str (tf/unparse jira-formatter created-at)
         ";"
         (get-comment-author c)
         comment-text)))

(defn format-title [issue]
  (if (:pull_request issue)
    (str "(PR #" (:number issue) ") " (:title issue))
    (:title issue)))

(defn get-labels
  [issue]
  (let [labels (map :name (:labels issue))
        with-dashes (map #(str/replace %1 \space \-) labels)]
    (str/join " " with-dashes)))

; :number 52 is a good one, lots of comments
; (def x (first (filter #(= (:number %) 52) (issues-with-extra-cached))))
;(map (juxt :created_at (comp :login :actor) :event :commit_id) (:event-contents x))


(defn filter-events [event-contents]
  (let [bad-events #{"subscribed" "head_ref_deleted" "head_ref_restored" "locked" "unlocked"}]
    (remove #(contains? bad-events (:event %)) event-contents)))

(defn issue2row [issue]
  (let [filtered-events (filter-events (:event-contents issue))
        all-comments (concat (:comment-contents issue)
                             filtered-events)
        allowed-comments (remove bad-mention? all-comments)
        trimmed-comments (take *maxcmt*
                               (sort-by :created_at allowed-comments))
        milestone (:title (:milestone issue))
        milestone-dashes (str/replace (or milestone "") \space \-)]
    (concat
      (vector
        (+ *issue-offset* (:number issue))
        (format-title issue)
        (cross-item-ref-replace (:body issue) *jira-project* *issue-offset*)
        (gh2jira (:created_at issue))
        (gh2jira (:updated_at issue))
        "Task" ; issue type
        milestone-dashes
        (if (= "closed" (:state issue)) "Closed" "Open")
        (if (= "closed" (:state issue)) "Fixed" "Unresolved")
        (get-user issue)
        (get-assignee issue)
        (get-labels issue))
      (map format-comment trimmed-comments)
      (repeat (- *maxcmt* (count trimmed-comments)) "")    ; pad out field count
    )))

(defn export-issues-to-file [issues filename]
  (let [issues-in-order (sort-by :number issues)]
    (with-open [out-file (io/writer filename)]
      (csv/write-csv
        out-file
        (concat
          [(columns)]
          (map issue2row issues-in-order))))))

;; Main

(defn print-usage []
  (println "Usage: lein2 run project_id")
  (println "project_id should correspond to a config file, e.g. 'my-project' for 'config-my-project.clj'"))

(defn load-config [config-id]
  (with-open [r (io/reader (str "config-" config-id ".clj"))]
    (read (java.io.PushbackReader. r))))

(defn process
  "main program"
  [config-id]
    (let [config (load-config config-id)]
    (binding [
            *config-id* config-id
            *ghuser* (:ghuser config)
            *ghproject* (:ghproject config)
            *auth* (:auth config)
            *maxcmt* (:maxcmt config)
            *user-map* (:user-map config)
            *ignore-mentions* (:ignore-mentions config)
            *jira-project* (:jira-project config)
            *issue-offset* (:issue-offset config)
            *git-base-url* (:git-base-url config)
            ]
    ; change to issues-with-extra-cached for faster development
    (let [issues (issues-with-extra)]
      (warn-missing-issues issues)
      (export-issues-to-file issues (str "JIRA-" *config-id* ".csv"))))))

(defn -main [& args]
  (when (empty? args)
    (print-usage)
    (System/exit 1))
  (process (first args)))

; To test from the REPL:
; (process "1")
