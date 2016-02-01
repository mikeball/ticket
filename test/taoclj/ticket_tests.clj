(ns taoclj.ticket-tests
  (:use clojure.test
        taoclj.ticket)
  (:import [java.time Instant LocalDateTime ZoneOffset Duration]))


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


(deftest nil-tickets-are-handled
  (is (nil? (let [read (make-reader secret-key)]
              (read nil (Instant/now))))))



;; *******************************************



(defn test-cookie [settings-to-merge]
  (issue-cookie
   (merge
    {:secret-key    secret-key
     :cookie-name   "id"
     :value  123
     :expires       (-> (Instant/now)
                        (.plus (Duration/ofDays 365)))}

     settings-to-merge)))


(deftest cookie-name-is-set
  (is (-> (test-cookie {:cookie-name "aaa"})
          (contains? "aaa"))))


(deftest cookie-http-only-is-set
  (are [given expected]
       (= expected
          (get-in (test-cookie {:http-only given})
                  ["id" :http-only]))
       true true
       false false ))

(deftest cookie-secure-is-set
  (are [given expected]
       (= expected
          (get-in (test-cookie {:secure given})
                  ["id" :secure]))
       true true
       false false ))



(deftest cookie-expiration-is-set
  (let [expires (-> (LocalDateTime/of 2016 1 30 14 11 12)
                    (.toInstant (ZoneOffset/UTC)))]
    (is (-> (test-cookie {:expires expires})
            (get-in ["id" :expires])
            (= "Sat, 30 Jan 2016 14:11:12 GMT")))))



(deftest cookie-value-is-round-tripped
    (are [value]
         (= value
            (let [now     (Instant/now)
                  expires (-> now (.plus (Duration/ofHours 1)))

                  read   (make-reader secret-key)
                  cookie (test-cookie {:cookie-name "id"
                                        :value       value
                                        :expires     expires
                                        :secret-key  secret-key})]
              (read (get-in cookie ["id" :value]) now)))

         1234
         "test" ))


;; (run-tests *ns*)














