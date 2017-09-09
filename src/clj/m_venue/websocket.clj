(ns m-venue.websocket
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET]]
            [clojure.string :as string]
            [m-venue.authentication :refer [get-user is-editor]]
            [nginx.clojure.core :as ncc]
            [spec-serialize.impl :as tf]))

(defonce subscriptions (atom {}))
(defonce edit-subscriptions (atom {}))

(defn subscribe
  [id open-f message-f close-f]
  (swap! subscriptions #(assoc % id [open-f message-f close-f]))
  (swap! edit-subscriptions #(assoc % id [open-f message-f close-f])))

(defn edit-subscribe
  [id open-f message-f close-f]
  (swap! edit-subscriptions #(assoc % id [open-f message-f close-f])))

(defn on-open!
  [ch uid edit-only]
  (let [subs-map (if edit-only @edit-subscriptions @subscriptions)]
    (doseq [[id [open-f message-f close-f]] subs-map]
      (let [result (open-f ch uid)]
        (log/debug result)))))

(defn on-message-handler
  [[handled? ch uid msg] id [open-f message-f close-f]]
  (if (and (false? handled?) (string/starts-with? msg id))
    [true (message-f ch uid (subs msg 3))]
    [handled? ch msg]))

(defn on-message!
  [ch uid msg edit-only]
  (let [subs-map (if edit-only @edit-subscriptions @subscriptions)
        result (reduce-kv on-message-handler [false ch uid msg] subs-map)]
    (if
      (false? (first result))
      (log/warn "message was not handled by one of the subscribe handlers:" msg "from" uid))
      (log/debug "message was handled successfully")
    ))

(defn on-close!
  [ch uid reason edit-only]
  (let [subs-map (if edit-only @edit-subscriptions @subscriptions)]
  (doseq [[id [open-f message-f close-f]] subs-map]
    (let [result (close-f ch uid reason)]
      (log/debug result)))))

(defroutes web-socket-routes
           ;; public Websocket server endpoint
           (GET "/public" [:as req]
             (let [ch (ncc/hijack! req true)
                   uid (get-user req)]
               (when (ncc/websocket-upgrade! ch true)
                 (ncc/add-aggregated-listener! ch 500
                                               {:on-open    (fn [ch] (on-open! ch uid false))
                                                :on-message (fn [ch msg] (on-message! ch uid msg false))
                                                :on-close   (fn [ch reason] (on-close! ch uid reason false))})
                 {:status 200 :body ch})))
           ;; edit Websocket server endpoint
           (GET "/editable" [:as req]
             (let [ch (ncc/hijack! req true)
                   uid (get-user req)]
               (if (and (is-editor uid) (ncc/websocket-upgrade! ch true))
                 (do
                   (ncc/add-aggregated-listener! ch 500
                                                 {:on-open    (fn [ch] (on-open! ch uid false))
                                                  :on-message (fn [ch msg] (on-message! ch uid msg false))
                                                  :on-close   (fn [ch reason] (on-close! ch uid reason false))})
                   {:status 200 :body ch})
                 {:status 403 :body "Your not logged in as an user with edit rights"}))))