(ns m-venue.repo-bridge
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [m-venue.repo :refer [get-string set-string! for-all]]
            [m-venue.websocket :refer [edit-subscribe]]
            [nginx.clojure.core :refer [send!]]
            [spec-serialize.core :refer [ser-map]]))

(edit-subscribe
  "get"
  (fn [_ uid]
    (str "user: " uid " is ready to get data"))
  (fn [ch _ kkey]
    (log/debug "key from get is" kkey)
    (if-let [[k key] (string/split kkey #"-" 2)]
      (send! ch (str "set" kkey ":" (get-string k key)) true false)))
  (fn [_ uid _]
    (str "user: " uid " left! Doesn't get data anymore")))

(edit-subscribe
  "set"
  (fn [_ uid]
    (str "user: " uid " is ready to set data"))
  (fn [_ _ msg]
    (log/debug "message at set is" msg)
    (if-let [[kkey data] (string/split msg #":" 2)]
      (if-let [[k key] (string/split kkey #"-" 2)]
        (set-string! k key data))))
  (fn [_ uid _]
    (str "user: " uid " left! Doesn't set data anymore")))

(edit-subscribe
  "ls-"
  (fn [_ uid]
    (str "user: " uid " is ready to submit local storage data"))
  (fn [ch _ msg]
    (log/debug "message at ls- is" msg)
    (if
      (< (bigdec msg) 2)
      (for-all (fn [k key [spec map]]
                 send! ch (str "set" (name k) "-" key ":" [spec (ser-map spec map)]) true false))))
  (fn [_ uid _]
    (str "user: " uid " left! Doesn't send local storage status data anymore")))
