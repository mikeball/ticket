(ns taoclj.ticket.signing
  (:require [taoclj.ticket.util :refer [slice]]
            [taoclj.ticket.encoding   :refer [encode-hex decode-hex]])
  (:import  [java.security MessageDigest]
            [javax.crypto Mac]
            [javax.crypto.spec SecretKeySpec] ))



(defn derive-signing-key [secret-key]
  (.digest (MessageDigest/getInstance "SHA-1")
                             secret-key))


(defn get-signature [#^bytes payload #^bytes secret-key]
  (let [signing-key (derive-signing-key secret-key)
        algo "HmacSHA1"
        mac (Mac/getInstance algo)
        _ (.init mac (SecretKeySpec. signing-key algo))]
    (.doFinal mac payload) ) )


(defn signature-valid?
  "Checks validity/equality of HmacSHA256 signatures in constant time."
  [#^bytes given #^bytes good ]
  (if (= 20 (count good) (count given))
    (zero? (reduce bit-or (map bit-xor good given)))
    false))




(defn sign-payload
  "generate an HmacSHA1 and return it as byte array."
  [#^bytes payload #^bytes secret-key]
  (byte-array (concat (get-signature payload secret-key)
                      payload)) )


(get-signature (.getBytes "t")
               (decode-hex "6e3c528eb078df50b11e1a78067456a8"))




(defn unpack-signed-payload
  "Checks validity/equality of HmacSHA1 signatures on a payload in constant time.
   Returns payload if signature is valid, nil otherwise."
  [#^bytes sig+payload #^bytes secret-key]

  (if (< 20 (count sig+payload))
    (let [payload-bytes (slice sig+payload 20 (count sig+payload))]

      (if (signature-valid? (get-signature payload-bytes secret-key)
                            (slice sig+payload 0 20))
        (slice sig+payload 20 (count sig+payload))) )))


(unpack-signed-payload
  (decode-hex "c460cd2c7d6143e24cb265b235630d4d6415abb274657374")
  (decode-hex "f7e8fb6cd89cb9b8a1861d11fd8c3ed3")
 )











