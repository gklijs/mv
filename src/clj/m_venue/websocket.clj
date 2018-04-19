(ns m-venue.websocket
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET]]
            [clojure.string :as string]
            [m-venue.authentication :refer [get-user is-editor]]
            [m-venue.image-processing :refer [process]]
            [nginx.clojure.core :as ncc]))

(defonce edit-subscriptions (atom {}))

(defn edit-subscribe
  [id open-f message-f close-f]
  (swap! edit-subscriptions #(assoc % id [open-f message-f close-f])))

(defn on-open!
  [ch uid]
  (doseq [[_ [open-f _ _]] @edit-subscriptions]
    (let [result (open-f ch uid)]
      (log/debug result))))

(defn on-message-handler
  [[handled? ch uid msg] id [_ message-f _]]
  (if (and (false? handled?) (string/starts-with? msg id))
    [true (message-f ch uid (subs msg 3))]
    [handled? ch uid msg]))

(defn process-binary
  [ch binary]
  (if-let [img-msgs (process binary)]
    (doseq [msg img-msgs] (ncc/send! ch msg true false))))

(defn on-message!
  [ch uid msg]
  (cond
    (string? msg)
    (let [result (reduce-kv on-message-handler [false ch uid msg] @edit-subscriptions)]
      (if
        (false? (first result))
        (log/warn "message was not handled by one of the subscribe handlers:" msg "from" uid))
      (log/debug "message" msg "was handled successfully with result" (second result)))
    (not (is-editor uid)) (log/warn "non-editor tried to send bytes instead of string, uid: " uid)
    (bytes? msg) (process-binary ch msg)
    :else (process-binary ch (.array msg))))

(defn on-close!
  [ch uid reason]
  (doseq [[_ [_ _ close-f]] @edit-subscriptions]
    (let [result (close-f ch uid reason)]
      (log/debug result))))

(defroutes web-socket-routes
           ;; edit Websocket server endpoint
           (GET "/editable" [:as req]
             (let [ch (ncc/hijack! req true)
                   uid (get-user req)]
               (if (and (is-editor uid) (ncc/websocket-upgrade! ch true))
                 (do
                   (ncc/add-aggregated-listener! ch (* 5 1024 1024)
                                                 {:on-open    (fn [ch] (on-open! ch uid))
                                                  :on-message (fn [ch msg] (on-message! ch uid msg))
                                                  :on-close   (fn [ch reason] (on-close! ch uid reason))
                                                  :on-error   (fn [_ status] (log/warn "error on edit web socket with status" status))})
                   {:status 200 :body ch})
                 (do
                   (log/debug "user with uid" uid "tried to connect to the editable web socket, but doesn't have the rights")
                   (ncc/send-response! ch {:status 403}))))))
