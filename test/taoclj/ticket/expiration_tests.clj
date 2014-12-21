(ns taoclj.ticket.expiration-tests
  (:require [taoclj.ticket.encoding :refer [long-to-bytes bytes-to-long]])
  (:use clojure.test
        taoclj.ticket.expiration)
  (:import [java.time Instant]))



(deftest timestamps-are-enforced
  (let [expiry (Instant/now)]
    (is (nil? (unpack-timestamped-payload
                (timestamp-payload (long-to-bytes 444) expiry)
                expiry)))) )




