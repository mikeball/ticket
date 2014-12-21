(ns taoclj.ticket.signing-tests
  (:require [taoclj.ticket.encoding :refer [decode-hex]])
  (:use clojure.test
        taoclj.ticket.signing))



(deftest bad-signatures-are-detected
  (is (not (nil? (unpack-signed-payload
                   (decode-hex "c460cd2c7d6143e24cb265b235630d4d6415abb274657374")
                   (decode-hex "f7e8fb6cd89cb9b8a1861d11fd8c3ed3") ))))
  (is (nil? (unpack-signed-payload
              (decode-hex "0060cd2c7d6143e24cb265b235630d4d6415abb274657374")
              (decode-hex "f7e8fb6cd89cb9b8a1861d11fd8c3ed3") ))) )

