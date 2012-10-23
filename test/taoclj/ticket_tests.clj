(ns taoclj.ticket-tests
  (:use clojure.test
        taoclj.ticket)
  (:require [clj-time.core :as time]
            [clj-time.format :as time-format]))


(deftest byte-are-encoded
  (is (= (encode (.getBytes "test")) "dGVzdA==")))

(deftest strings-are-decoded
  (is (= (String. (decode "dGVzdA==")) "test")))



(def clear-text "test")
(def key "FR7u7rt7YI60oSUnD8N+uA==")
(def iv "h6AQnkjm3PXYHqLIZLX2bg==")


(deftest clear-text-is-encrypted-with-iv-prepended
  (is (= "h6AQnkjm3PXYHqLIZLX2buOE7WLVajLDhoc/yebiRWU="
         (encode (encrypt (decode key) (decode iv) clear-text)))))

(deftest data-is-decrypted
  (is (= clear-text (String. (decrypt (decode key) 
                                      (decode iv) 
                                      (decode "44TtYtVqMsOGhz/J5uJFZQ=="))))))

(deftest signature-is-generated
  (let [sig (signature (decode key) (decode "h6AQnkjm3PXYHqLIZLX2buOE7WLVajLDhoc/yebiRWU="))]
    (is (= 32 (count sig)))
    (is (= "/3AD23f5ynhfT+f69scmAbTDCDzblXNGn2z0B4oS5hA=" (encode sig)))))

(deftest signatures-of-different-lengths-dont-pass-and-handle-nils
  (are [good given] (false? (signature-valid? good given))
       (.getBytes "aaa") (.getBytes "aa")
       nil (.getBytes "aaa")
       (.getBytes "aaa") nil))



(deftest invalid-signatures-are-detected
  (let [good-sig (decode "XuzimwPIEW3DPyppoUxF/CwiT2zA0V/iz+qgnRNpIKI=")
        bad-sig (decode "oFXsHiUslECsPavGx0BEmR9Ge54Zh/ktH9tTuoXUmCQ=")]
    (are [good given expected] (= expected (signature-valid? good given))
         good-sig (.getBytes "abc") false
         good-sig bad-sig false
         good-sig good-sig true
         good-sig nil false
         good-sig (.getBytes "a") false)))


(def good-ticket "/3AD23f5ynhfT+f69scmAbTDCDzblXNGn2z0B4oS5hCHoBCeSObc9dgeoshktfZu44TtYtVqMsOGhz/J5uJFZQ==")

(deftest ticket-is-packed
  (is (= good-ticket (pack (decode key) (decode iv) clear-text))))


(deftest tickets-signatures-are-verified-when-unpacked
  (let [bad-ticket "/3AA23f5ynhfT+f69scmAbTDCDzblXNGn2z0B4oS5hCHoBCeSObc9dgeoshktfZu44TtYtVqMsOGhz/J5uJFZQ=="]
    (is (nil? (unpack key bad-ticket)))
    (is (not (nil? (unpack key good-ticket))))))

(deftest ticket-contents-are-unpacked
  (is (= clear-text (unpack key good-ticket))))



(deftest text-is-returned-on-still-valid-issued-tickets
  (let [now (time/now)
        tkt (issue key clear-text (time/plus now (time/minutes 2)))]
    (is (= clear-text (get-text key tkt)))))

(deftest get-text-returns-expected-values
  (are [minutes-offset expected] (let [now (time/now)
                                       expires-at  (time/plus now (time/minutes minutes-offset))
                                       tkt (issue key clear-text expires-at)]
                                   (is (= expected (get-text key tkt))))
       1 clear-text
       0 nil))

(deftest get-id-returns-expected-values
  (are [minutes-offset expected] (let [now (time/now)
                                       expires-at  (time/plus now (time/minutes minutes-offset))
                                       tkt (issue key "111" expires-at)]
                                   (is (= expected (get-id key tkt))))
       1 111
       0 nil))