(ns taoclj.ticket
  (:require [taoclj.ticket.encoding   :refer [encode-hex decode-hex]]
            [taoclj.ticket.packaging  :refer [package unpack]]
            [taoclj.ticket.expiration :refer [timestamp-payload unpack-timestamped-payload]]
            [taoclj.ticket.crypto     :refer [random-bytes encrypt-payload decrypt-payload]]
            [taoclj.ticket.signing    :refer [sign-payload unpack-signed-payload]])
  (:import [java.time Instant ZoneId]
           ; [java.time.temporal ChronoUnit]
           [java.time.format DateTimeFormatter]))




; should key generation be part of ticket library?
; Where should key generation be? taoclj.tao.crypto?
(defn gen-key
  "Generates a random key of given size in bytes, returns hex encoded string."
  [size]
  (encode-hex (random-bytes size)))

; (gen-key 16)


(defn issue
  "Creates a new encrypted ticket. The ticket will be signed,
   then the expiration and payload encrypted.

  payload    : the value you wish to store in the ticket
  expiry     : java.time.Instant after which ticket is no longer valid
  secret-key : HEX encoded private key string

  returns the ticket as a string."
  [payload expiry secret-key]

  ; perhaps validate the secret-key length, expiry, payload...

  (let [secret-key-bytes (decode-hex secret-key)]
    (-> (package payload)
        (timestamp-payload expiry)
        (encrypt-payload secret-key-bytes)
        (sign-payload secret-key-bytes)
        (encode-hex) )))


; (issue 1234 (java.time.Instant/MAX) "6e3c528eb078df50b11e1a78067456a8"
;        )



; could we cache or memoize the result somehow?
; perhaps we should pass in the secret-key as a byte array, handle decoding outside of this lib?
; if we do this, we can collapse this into a single function, which seems simpler
(defn make-reader [^String secret-key]
  (let [secret-key-bytes (decode-hex secret-key)]
    (fn [^String ticket ^Instant as-of]
      (if ticket
        (some-> (decode-hex ticket)
                (unpack-signed-payload secret-key-bytes)
                (decrypt-payload secret-key-bytes)
                (unpack-timestamped-payload as-of)
                (unpack))))))


(comment
  (let [read (make-reader "6e3c528eb078df50b11e1a78067456a8")]
    (read "cbf12593e27870902dacecd4aa7b98d070a2e6c886e26d6b80a9adddc3a7a9e17c6db6374c9e63065001190a42e0ab80436d77753196aa3c92fa4d073b40973a58baf3c2"
          (Instant/now)) ))





(defn issue-cookie [settings]

  (let [cookie-name (:cookie-name settings)]

    (if-not (string? cookie-name) ;; todo check for empty string
      (throw (Exception. ":cookie-name key must be string specified in settings map!")))

    {(:cookie-name settings)
     {:http-only (:http-only settings)
      :secure (:secure settings)

      :expires
      (if-let [expires (:expires settings)]
        (-> expires
            (.atZone (ZoneId/of "UTC"))
            (.format (DateTimeFormatter/RFC_1123_DATE_TIME))))

      :value
      (issue (:value settings)
             (:expires settings)
             (:secret-key settings))

      }}))



(comment
  (issue-cookie
   {:secret-key    "key string"
    :cookie-name   "id"
    :cookie-value  123
    :http-only     true ; default to true
    :secure        false ; default to true
    :expires       (-> (LocalDateTime/of 2016 1 30 14 11 12)
                       (.toInstant (java.time.ZoneOffset/UTC)))
    }))



; expired cookie

(defn expired-cookie [cookie-name]
  {cookie-name {:value "expired"
                :expires "Thu, 01 Jan 1970 00:00:00 UTC"}})
