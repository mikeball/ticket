(ns taoclj.ticket.util
  (:import [java.util Arrays]))



;; ; array utilities
(defn slice [#^bytes bytes ^Long start ^Long end]
  (Arrays/copyOfRange bytes start end))



