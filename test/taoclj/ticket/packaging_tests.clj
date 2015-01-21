(ns taoclj.ticket.packaging-tests
  (:use clojure.test
        taoclj.ticket.packaging))



(deftest packaged-values-are-round-tripped
  (are [v] (= v (unpack (package v)))
       1234
       "test"
       (java.lang.Integer. 3456)

       ))


