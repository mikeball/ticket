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

- Ticket byte array is then Hex encoded as a string
- HMAC signature is checked in constant time
- IV is generated as a hash of the secret key for ease of use
- AES 128 is deemed strong enough for short term ticket encryption


## Installation

Add the following dependency to your `project.clj` file:

```clojure
[org.taoclj/ticket "0.2.0"]
```



## Usage

```clojure
(require '[taoclj.ticket :as ticket])
(require '[clj-time.core :as time])

;; generate a key
(def my-key (ticket/generate-key))
=> "your-randomly-generated-128bit-key-string"


```

### Issue Tickets
```clojure
;; issue a ticket valid for 2 minutes with value of "123".
(def my-ticket (ticket/issue my-key
                             "123"
                             (time/plus (time/now) (time/minutes 2))))
=> "your-encrypted-signed-and-encoded-ticket-string"


;; Easily retrieve the string value from a ticket.
(ticket/get-text my-key my-ticket)
=> "123"

;; If the ticket value is an integer, easily retrieve the numeric value.
(ticket/get-id my-key my-ticket)
=> 123


;; After expiration of ticket, or if the ticket has been tampered with...
(ticket/get-text my-key my-ticket)
=> nil

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
