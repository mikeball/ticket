(ns taoclj.ticket.packaging
  (:require [taoclj.ticket.encoding :refer [long-to-bytes bytes-to-long]]
            [taoclj.ticket.util :refer [slice]]))


(def long-identifier (byte 1))

(def string-identifier (byte 2))

(def string-encoding "UTF-8")


(defn package
  "Packages the type specifier and payload bytes, returns the resulting byte array."
  [payload]

  (let [cls (class payload)]
    (cond (= cls java.lang.Long)
          (byte-array (cons long-identifier
                            (long-to-bytes payload)))

          (= cls java.lang.String)
          (byte-array (cons string-identifier
                      (.getBytes payload string-encoding)))

          (= cls java.lang.Integer)
          (byte-array (cons long-identifier
                            (long-to-bytes (.longValue payload))))

          :default
          (throw (Exception. "Payload type not supported!"))  )))

; (package "test")
; (package 555)


(defn unpack
  "Unpacks the payload byte array, converts to specified type and returns."
  [#^bytes payload]

  (let [type-id (first payload)]
    (cond (= type-id long-identifier)
          (bytes-to-long (slice payload 1 9))


          (= type-id string-identifier)
          (String. (slice payload 1 (count payload)) string-encoding)

          :default nil )))

; (unpack (package 444))
; (unpack (package "xxx"))


















;; (defn timestamp-id
;;   "Package up the id with expiration instant into a byte array"
;;   [^Long id ^Instant expiry]
;;   (timestamp-payload (long-to-bytes id) expiry))

;; ; (timestamp-id 1234 (Instant/MAX))


;; (defn unpack-timestamped-id
;;   "Unpack id from packaged expiration instant and payload"
;;   [#^bytes expiry+payload ^Instant as-of]
;;   (if-let [id (unpack-timestamped-payload expiry+payload as-of)]
;;     (bytes-to-long id)))


;; (decode-hex "00701cd2fa9578ff00000000000004d2")

;; (unpack-timestamped-id
;;    (decode-hex "00701cd2fa9578ff00000000000004d2")
;;    (Instant/now))

;; (unpack-timestamped-id
;;  (timestamp-id 222 (Instant/MAX))
;;                         (Instant/now))


;; (.plus (Instant/now) (java.time.Duration/ofMinutes 2))
