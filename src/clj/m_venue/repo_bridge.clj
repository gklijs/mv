(ns m-venue.repo-bridge
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [m-venue.repo :refer [get-string set-string!]]
            [m-venue.websocket :refer [edit-subscribe]]
            [nginx.clojure.core :refer [send!]]))

(edit-subscribe
  "get"
  (fn [ch uid]
    (str "user: " uid " is ready to get data"))
  (fn [ch uid key]
    (log/debug "key from get is" key)
    (if-let [data (get-string key)]
    (send! ch (str "get" key ":" data) true false)
    (send! ch (str "get" key ":" nil) true false)))
  (fn [ch uid reason]
    (str "user: " uid " left! Doesn't get data anymore")))

(edit-subscribe
  "set"
  (fn [ch uid]
    (str "user: " uid " is ready to set data"))
  (fn [ch uid msg]
    (log/debug "message at set is" msg)
    (if-let [[key data] (string/split msg #":" 2)]
      (set-string! key data)))
  (fn [ch uid reason]
    (str "user: " uid " left! Doesn't get data anymore")))
