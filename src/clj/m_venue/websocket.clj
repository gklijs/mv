(ns m-venue.websocket
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET]]
            [clojure.string :as string]
            [m-venue.authentication :refer [get-user]]
            [nginx.clojure.core :as ncc]
            [spec-serialize.impl :as tf]))

(defonce subscriptions (atom {}))

(defn subscribe
  [id open-f message-f close-f]
  (swap! subscriptions #(assoc % id [open-f message-f close-f])))

(defn on-open!
  [ch uid]
  (doseq [[id [open-f message-f close-f]] @subscriptions]
    (let [result (open-f ch uid)]
      (log/debug result))))

(defn on-message-handler
  [[handled? ch uid msg] id [open-f message-f close-f]]
  (log/debug "--------msg " msg " by " uid)
  (if (and (false? handled?) (string/starts-with? msg id))
    [true (message-f ch uid (subs msg 3))]
    [handled? ch msg]))

(defn on-message!
  [ch uid msg]
  (let [result (reduce-kv on-message-handler [false ch uid msg] @subscriptions)]
    (if
      (false? (first result))
      (println (str "message was not handled by one of the subscribe handlers: " msg " from " uid)))
    ))

(defn on-close!
  [ch uid reason]
  (doseq [[id [open-f message-f close-f]] @subscriptions]
    (let [result (close-f ch uid reason)]
      (log/debug result))))

(defroutes web-socket-route
           ;; Websocket server endpoint
           (GET "/ws" [:as req]
             (let [ch (ncc/hijack! req true)
                   uid (get-user req)]
               (when (ncc/websocket-upgrade! ch true)
                 (ncc/add-aggregated-listener! ch 500
                                               {:on-open    (fn [ch] (on-open! ch uid))
                                                :on-message (fn [ch msg] (on-message! ch uid msg))
                                                :on-close   (fn [ch reason] (on-close! ch uid reason))})
                 {:status 200 :body ch}))))
