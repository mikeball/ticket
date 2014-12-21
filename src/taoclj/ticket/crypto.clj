(ns taoclj.ticket.crypto
  (:require [taoclj.ticket.util :refer [slice]])
  (:import [java.security SecureRandom]
           [javax.crypto Cipher Mac]
           [javax.crypto.spec IvParameterSpec SecretKeySpec] ))


(defn random-bytes
  "Generates a cryptographically random byte array of given size."
  [size]
  (let [bytes (byte-array size)]
    (.nextBytes (SecureRandom.) bytes)
    bytes))


(defn- init-cipher [^Integer op-mode #^bytes key #^bytes iv]
  (let [cipher (Cipher/getInstance "AES/CBC/PKCS5Padding")]
    (.init cipher op-mode (SecretKeySpec. key "AES") (IvParameterSpec. iv))
    cipher))


(defn encrypt-payload
  "Encrypts the given clear-bytes array and returns cipher-bytes array with the iv prepended."
  [#^bytes clear-bytes #^bytes secret-key]

  (let [iv (random-bytes 16)]
    (->> (.doFinal (init-cipher Cipher/ENCRYPT_MODE secret-key iv)
                   clear-bytes)
         (concat iv)
         (byte-array) )))


(defn decrypt-payload
  "returns byte array of decrypted cipher-bytes"
  [#^bytes iv+cipher-bytes #^bytes secret-key]

  (let [iv           (slice iv+cipher-bytes 0  16)
        cipher-bytes (slice iv+cipher-bytes 16 (count iv+cipher-bytes))]
    (-> (init-cipher Cipher/DECRYPT_MODE secret-key iv)
        (.doFinal cipher-bytes) )))


