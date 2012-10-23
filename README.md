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
(require '[org.taoclj.ticket :as ticket])
(require '[clj-time.core :as time])

;; generate a key

(def key "FR7u7rt7YI60oSUnD8N+uA==")

;; issue a ticket valid for 1 minute with a string value of "myid"
(ticket/issue key 
              "myid" 
              (time/plus (time/now) (time/minutes 2)))

=> "/3AD23f5ynhfT+f69scmAbTDCDzblXNGn2z0B4oS5hCHoBCeSObc9dgeoshktfZu44TtYtVqMsOGhz/J5uJFZQ=="




;; Retrieve the text value from a ticket. If the ticket has expired,
;; or been tampered with, nil is returned.

(ticket/get-text key myticket)
=> "myid"



;; Two minute after issuence of ticket...
(ticket/get-text key myticket)
=> nil




;; if you place a integer value inside of a ticket, you can easily get that back as well.
;; assuming you issued a ticket with a value of "123"

(ticket/get-id key myticket)

=> 123


```






## License

Copyright © 2012 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.