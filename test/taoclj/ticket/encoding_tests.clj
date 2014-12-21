(ns taoclj.ticket.encoding-tests
  (:use clojure.test
        taoclj.ticket.encoding))



(deftest longs-are-round-tripped
  (are [n] (= n (bytes-to-long (long-to-bytes n)))
       1
       222
       1234))


(deftest hex-is-roundtripped
  (are [s] (= s
              (String. (decode-hex
                         (encode-hex (.getBytes s)))))
      "a" ))




; (run-tests *ns*)
