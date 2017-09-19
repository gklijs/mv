(ns m-venue.web-socket
  (:require [clojure.string :as string]))

(defonce ws-chan (atom nil))
(defonce subscriptions (atom {}))
(defonce reconnecting (atom false))
(defonce default-wait-time 5000)
(defonce max-wait-time 300000)
(defonce wait-time (atom default-wait-time))
(defonce ws-location (atom nil))
(defonce msg-queue (atom []))
(declare make-web-socket!)

(defn subscribe
  [id message-f]
  (swap! subscriptions #(assoc % id message-f)))

(defn msg-handler
  [[handled? msg] id message-f]
  (if (and (false? handled?) (string/starts-with? msg id))
    [true (message-f (subs msg 3))]
    [handled? msg]))

(defn receive-msg!
  [msg-event]
  (let [msg-data (.-data msg-event)
        result (reduce-kv msg-handler [false msg-data] @subscriptions)]
    (if
      (false? (first result))
      (println (str "message was not handled by one of the subscribe handlers: " msg-data)))
    ))

(defn delayed-reconnect!
  [error-or-close-event]
  (reset! ws-chan nil)
  (js/console.log error-or-close-event)
  (when (not @reconnecting)
    (reset! reconnecting true)
    (js/setTimeout #(do (make-web-socket!)
                        (if (< @wait-time max-wait-time) (swap! wait-time (fn [old-value] (* old-value 2))))
                        (reset! reconnecting false)) @wait-time)))

(defn make-web-socket! []
  (let [url (str "ws://" (-> js/window .-location .-host) @ws-location)]
    (if-let [chan (js/WebSocket. url)]
      (do
        (set! (.-onopen chan) (fn [] (println "opening connection to " url)))
        (set! (.-onmessage chan) receive-msg!)
        (set! (.-onerror chan) delayed-reconnect!)
        (set! (.-onclose chan) delayed-reconnect!)
        (reset! ws-chan chan))
      (throw (js/Error. "Websocket connection failed!")))))

(defn send-msg!
  [msg]
  (if (and (not (nil? @ws-chan)) (= (.-readyState @ws-chan) 1))
    (if
      (= 0 (count @msg-queue))
      (.send @ws-chan msg)
      (do
        (doseq [msg-from-q @msg-queue] (.send @ws-chan msg))
        (reset! msg-queue [])
        (.send @ws-chan msg)))
    (swap! msg-queue #(conj % msg))))

(defn init!
  "Initializes the websocket"
  [location]
  (reset! ws-location location)
  (make-web-socket!))
