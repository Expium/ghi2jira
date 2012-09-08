(ns ghijira.core-test
  (:use ghijira.core)
  (:use clojure.test))

(deftest should-identify-mising-issues
  (let [issues [{:number 3 :title "Third"} {:number 1 :title "First"}]]
    (is (= [2] (find-missing-issues issues))))
  (let [issues [{:number 3 :title "Third"} ]]
    (is (= [1 2] (find-missing-issues issues)))))
