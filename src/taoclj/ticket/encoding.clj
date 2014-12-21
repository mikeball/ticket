(ns taoclj.ticket.encoding
  (:import [java.nio ByteBuffer]
           [org.apache.commons.codec.binary Hex]))



(defn long-to-bytes
  "Suppy a java long, returns byte array."
  [^Long n]
  (-> (ByteBuffer/allocate (Long/BYTES))
      (.putLong n)
      (.array)))

(defn bytes-to-long
  "Supply a byte array, converts to Long."
  [^bytes bytes]
  (-> (ByteBuffer/allocate (Long/BYTES))
      (.put bytes)
      (.flip)
      (.getLong)))

; (bytes-to-long (long-to-bytes 5555))





(defn encode-hex
  "Supply a byte array, returns encoded string."
  [^bytes raw]
  (Hex/encodeHexString raw))

(defn decode-hex
  "Supply a hex encoded string, returns decoded byte array."
  [^String encoded]
  (Hex/decodeHex (.toCharArray encoded)))

; (encode-hex (.getBytes "test" "UTF-8"))
; (decode-hex "74657374")







