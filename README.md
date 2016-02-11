# Ticket

An encrypted ticketing library, primarily intended for use in cookie based authentication.


## Ticket Structure

```clojure
[[hmac-sha1-bytes]
 [iv-bytes]
 [encrypted-aes128-bytes
   [expiration-instant-bytes]
   [type-byte]
   [payload-bytes]]]
```

- Ticket byte array is Hex encoded as a string
- HMAC signature is checked in constant time
- IV is generated as a hash of the secret key
- AES 128 is deemed strong enough for short term encryption


## Installation
```clojure
[org.taoclj/ticket "0.2.0"]
```



## Preliminary Setup
```clojure
(require '[taoclj.ticket :as ticket])
(require '[taoclj.time :as time])

;; generate a key
(def secret-key (ticket/generate-key))
=> "your-randomly-generated-128bit-key-string"
```

### How to Issue Tickets
```clojure
;; issue a ticket valid for 2 hours with value of "abc".

(ticket/issue "abc"

              (time/now-plus 2 :hours)
              ;; or using java.time directly...
              ;; (.plus (java.time.Instant/now) (java.time.Duration/ofHours 2))

              secret-key)

=> "encrypted-signed-and-encoded-ticket-string"
```

### How to Read Tickets
```clojure

;; create a ticker reader
(def read-ticket (ticket/make-reader conf/cookie-key))

;; read the value out
(read-ticket "encrypted-signed-and-encoded-ticket-string"
             (time/now))

=> returns the value stored if ticket is valid
=> returns nil otherwise

```





### How to Issue Ring Cookies

```clojure
(ticket/issue-cookie
   {
    ;; from generated key above..
    :secret-key    "your-randomly-generated-128bit-key-string"
    :cookie-name   "my-cookie-name"

    ;; :value can be integer or string
    :value         123

    ;; A java.time.Instant after which the ticket should expire
    :expires       (time/now-plus 7 :days)

    :http-only     true
    :secure        false

    })
```


## License

Copyright © 2016 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.
