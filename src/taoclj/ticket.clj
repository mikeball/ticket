(ns taoclj.ticket
  (:require [clj-time.core :as time]
            [clj-time.format :as time-format])
  (:import [java.security SecureRandom]
           [javax.crypto Cipher Mac]
           [javax.crypto.spec IvParameterSpec SecretKeySpec]
           [org.apache.commons.codec.binary Base64]
           [java.util Arrays]
           [java.text SimpleDateFormat]))



(defn random-bytes
  "Generates a cryptographically random byte array of given size."
  [size] 
  (let [bytes (byte-array size)] (.nextBytes (SecureRandom.) bytes) bytes))


(defn encode [bytes]
  (String. (Base64/encodeBase64 bytes)))

(defn decode [^String encoded]
  (Base64/decodeBase64 (.getBytes encoded)))



(defn- init-cipher [^Integer op-mode #^bytes key #^bytes iv]
  (let [cipher (Cipher/getInstance "AES/CBC/PKCS5Padding")]
    (.init cipher op-mode (SecretKeySpec. key "AES") (IvParameterSpec. iv))
    cipher))

(defn encrypt
  "Encrypts the given string and returns a byte array with the iv prepended."
  [#^bytes key #^bytes iv ^String clear-text]
  (->> (.doFinal (init-cipher Cipher/ENCRYPT_MODE key iv)
                 (.getBytes clear-text))
       (concat iv)
       (byte-array)))

(defn decrypt
  "returns byte array of decrypted data"
  [#^bytes key #^bytes iv #^bytes cipher-text]
  (.doFinal (init-cipher Cipher/DECRYPT_MODE key iv) 
            cipher-text))



(defn signature
  "generate an HmacSHA256 and return it as byte array"
  [#^bytes key #^bytes data]
  (let [algo "HmacSHA256"
        mac (Mac/getInstance algo)
        _ (.init mac (SecretKeySpec. key algo))]
    (.doFinal mac data)))


(defn signature-valid?
  "Checks validity/equality of HmacSHA256 signatures in constant time."
  [#^bytes good #^bytes given]
  (if (= 32 (count good) (count given)) 
    (zero? (reduce bit-or (map bit-xor good given)))
    false))


(defn pack
  "Packages up an encrypted value and prepends a signature. Returns base64 encoded string."
  ([key iv clear-text]
     (let [cipher-bytes (encrypt key iv clear-text)
           signature-bytes (signature key cipher-bytes)]
       (-> (concat signature-bytes cipher-bytes)
           (byte-array)
           (encode)))))


(defn slice [#^bytes bytes ^Integer start ^Integer end]
  (Arrays/copyOfRange bytes start end))


(defn unpack           ;; could we cache or memoize this?
  "decodes the ticket, verifies signature, decrypts then returns the raw contents.
   Returns nil on any failure."
  [^String key ^String tkt]
  (let [key-bytes (decode key)
        tkt-bytes (decode tkt)
        data-bytes (slice tkt-bytes 32 (count tkt-bytes))]
       (if (signature-valid? (signature key-bytes data-bytes) (slice tkt-bytes 0 32))
         (String. (decrypt key-bytes 
                           (slice data-bytes 0 16)
                           (slice data-bytes 16 (count data-bytes)))))))




(def expiration-format (time-format/formatter "yyyyMMddHHmm"))

(defn issue [key val expires-at]
  (pack (decode key) 
        (random-bytes 16) 
        (str val (time-format/unparse expiration-format expires-at))))


(defn get-text
  "Returns the text value from ticket, nil if ticket is invalid or expired."
  [^String key ^String tkt]
  (let [raw (unpack key tkt)
        split-at (- (.length raw) 12)
        expires-at (time-format/parse expiration-format (.substring raw split-at))]
    (if (time/before? (time/now) expires-at)
      (.substring raw 0 split-at))))


(defn get-id
  "Returns a parsed integer of cookie value if possible, nil otherwise."
  [^String key ^String tkt]
  (let [txt (get-text key tkt)]
    (if txt
      (try (Integer. txt) (catch Exception e nil)))))


(defn generate-key []
  (encode (random-bytes 16)))