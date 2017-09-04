(ns m-venue.websocket
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(defonce ws-chan (atom nil))
(defonce subscriptions (atom {}))
(defonce reconnecting (atom false))
(defn make-web-socket! [])

(defn subscribe
  [validation-f execution-f]
  (swap! subscriptions #(assoc % validation-f execution-f)))

(defn msg-handler
  [[handled? msg] validation-f execution-f]
  (if (and (false? handled?) (validation-f msg))
    (do
      (execution-f msg)
      [true msg])
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
  (println "lost connection, will try to reconnect in 15 seconds")
  (if (not @reconnecting)
    (do
      (reset! reconnecting true)
      (go
        (<! (timeout 15000))
        (make-web-socket!)
        (reset! reconnecting false)))
    ))

(defn make-web-socket! []
  (let [url (str "ws://" (-> js/window .-location .-host) "/ws")]
    (println "attempting to connect websocket")
    (if-let [chan (js/WebSocket. url)]
      (do
        (set! (.-onopen chan) (fn [] (println "successfully connected")))
        (set! (.-onmessage chan) receive-msg!)
        (set! (.-onerror chan) delayed-reconnect!)
        (set! (.-onclose chan) delayed-reconnect!)
        (reset! ws-chan chan)
        (println "Websocket connection established with: " url))
      (throw (js/Error. "Websocket connection failed!")))))

(defn send-msg!
  [msg]
  (if (and (not (nil? @ws-chan)) (= (.-readyState @ws-chan) 1))
    (.send @ws-chan msg)
    (go-loop []
             (if @ws-chan
               (let [ready-state (.-readyState @ws-chan)]
                 (cond
                   (= ready-state 0) (do (<! (timeout 1000)) (recur))
                   (= ready-state 1) (.send @ws-chan msg))
                 :default (println (str "Could not send: '" msg "' to the server")))
               (do (<! (timeout 1000)) (recur))))
    ))

(defn init!
  "Initializes the websocket"
  []
  (make-web-socket!))
