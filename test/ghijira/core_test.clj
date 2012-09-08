(ns ghijira.core-test
  (:use ghijira.core)
  (:use clojure.test))

(deftest should-identify-mising-issues-in-the-middle
  (let [issues [{:number 3 :title "Third"} {:number 1 :title "First"}]]
    (is (= [2] (find-missing-issues issues)))))

(deftest should-identify-missing-issues-from-number-1
  (let [issues [{:number 3 :title "Third"} ]]
    (is (= [1 2] (find-missing-issues issues)))))

;; End-to-end test
(deftest should-export-all-issues-in-proper-format-and-order
  (let [issue1 {:number 1 
                :title "First" 
                :body "First issue" 
                :created_at "2012-09-08T15:00:00Z"
                :updated_at "2012-09-08T15:15:00Z"
                :milestone "M1"
                :state "open" 
                :user "anne"
                :comment-contents []}
        issue2 {:number 2 
                :title "Second" 
                :body "Second issue" 
                :created_at "2012-09-08T16:00:00Z"
                :updated_at "2012-09-08T16:15:00Z"
                :milestone "M2"
                :state "closed" 
                :user "betty"
                :comment-contents [{:user "betty"
                                    :created_at "2012-09-08T16:13:00Z"
                                    :body "First comment by betty"}
                                   {:user "anne"
                                    :created_at "2012-09-08T16:15:00Z"
                                    :body "Second comment by anne"}]}
        issue3 {:number 3 
                :title "Third" 
                :body "Third issue" 
                :created_at "2012-09-08T17:00:00Z"
                :updated_at "2012-09-08T17:15:00Z"
                :milestone "M3"
                :state "open" 
                :user "anne"
                :comment-contents []}
        issues [issue3 issue1 issue2]] ; random order
    (binding [*maxcmt* 25]
      (export-issues-to-file issues "testdata/temp/out.csv")
      (is (= (slurp "testdata/expected_jira.csv") (slurp"testdata/temp/out.csv"))))))
