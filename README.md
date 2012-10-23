# ticket

An encrypted ticketing library, primarily intended for use in cookie based authentication.

Each ticket is encrypted with AES 128 bit encryption & initialization vector, then signed with SHA256 HMAC. Ticket expiration 

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[org.taoclj/ticket "0.0.1"]
```


## Usage

```clojure
(require '[taoclj.ticket :as ticket])
(require '[clj-time.core :as time])

;; generate a key
(def my-key "FR7u7rt7YI60oSUnD8N+uA==")

;; issue a ticket valid for 2 minutes with value of "123".
(def my-ticket (ticket/issue my-key 
                             "123" 
                             (time/plus (time/now) (time/minutes 2))))

=> "your-encrypted-signed-and-encoded-ticket-string"



;; Easily retrieve the string value from a ticket. 
(ticket/get-text my-key my-ticket)
=> "123"

;; If the ticket value is an Integer, easily retrieve that as well. 
(ticket/get-text my-key my-ticket)
=> 123


;; After expiration of ticket, or if the ticket has been tampered with...
(ticket/get-text key myticket)
=> nil




```






## License

Copyright © 2012 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.