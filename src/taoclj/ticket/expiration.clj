(ns taoclj.ticket.expiration
  (:require [taoclj.ticket.util :refer [slice]]
            [taoclj.ticket.encoding :refer [long-to-bytes bytes-to-long]])
  (:import [java.time Instant]))



(defn timestamp-payload
  "Prepends the supplied expiration date onto the payload byte array."
  [#^bytes payload ^Instant expiry]
  (byte-array (concat (long-to-bytes (.getEpochSecond expiry))
                      payload)) )

;; (timestamp-payload (long-to-bytes 444)
;;                    (Instant/now))
;; (timestamp-payload (.getBytes "test")
;;                    (Instant/now))



(defn unpack-timestamped-payload
  "Takes byte array of expiration and payload, returns payload byte array if
  the supplied time is before the packaged expiration time."
  [#^bytes expiry+payload ^Instant now]
  (let [expiry (bytes-to-long (slice expiry+payload 0 8))]
    (if (< (.getEpochSecond now) expiry)
      (slice expiry+payload 8 (count expiry+payload)))))

;; (bytes-to-long (unpack-timestamped-payload (timestamp-payload (long-to-bytes 444)
;;                                                (Instant/MAX))
;;                             (Instant/now)
;;  ))
;; (unpack-timestamped-payload (timestamp-payload (long-to-bytes 444)
;;                                                (Instant/MAX))
;;                             (Instant/now)
;;  )






