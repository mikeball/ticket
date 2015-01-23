(ns taoclj.ticket-tests
  (:use clojure.test
        taoclj.ticket)
  (:import [java.time Instant]))


(def secret-key "f7e8fb6cd89cb9b8a1861d11fd8c3ed3")


(deftest tickets-are-round-tripped
  (are [v] (= v
              (let [read (make-reader secret-key)]
                (read (issue v (Instant/MAX) secret-key)
                      (Instant/now)) ))
       1234
       "test"
       ))



(deftest tickets-expire
  (is (nil?  (let [read (make-reader secret-key)
                   at   (Instant/now)]
               (read (issue "test" at secret-key)
                     at )))))


(deftest bad-keys-are-ignored
  (is (nil?  (let [read (make-reader secret-key)]
               (read (issue "test" (Instant/MAX) "ffe8fb6cd89cb9b8a1861d11fd8c3ed3")
                     (Instant/now))))))


(deftest nil-cookies-are-handled
  (is (nil? (let [read (make-reader secret-key)]
              (read nil (Instant/now))))))














