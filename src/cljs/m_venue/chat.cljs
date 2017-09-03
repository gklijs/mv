(ns m-venue.chat
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [clojure.string :as string]
            [goog.dom :as gdom])
  (:import goog.History))

(defonce ws-chan (atom nil))
(defonce message-counter (atom 0))

(defn receive-msg!
  [msg-event]
  (let [msg-data (.-data msg-event)
        color (cond
                    (string/ends-with? msg-data "[enter!]") "is-warning"
                    (string/ends-with? msg-data "[left!]") "is-danger"
                    :else "is-info")
        li-item (. js/document createElement "p")
        ]
    (set! (.-className li-item) (str "notification tile " color))
    (set! (.-id li-item) (str "chat-message-" @message-counter))
    (dom/set-text li-item msg-data)
    (dom/insert-at (dom/get-element :board) li-item 1)
    (if
      (> @message-counter 4)
      (gdom/removeNode (dom/get-element (str "chat-message-" (- @message-counter 5)))))
    (swap! message-counter inc)
    ))

(defn make-web-socket! [])

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

(defn delayed-reconnect!
  [error-or-close-event]
  (reset! ws-chan nil)
  (println "lost connection, will try to reconnect in 10 seconds")
  (go
    (<! (timeout 10000))
    (make-web-socket!)))

(defn make-web-socket! []
  (let [url (str "ws://" (-> js/window .-location .-host) "/chat")]
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

(defn send-chat-message
  []
  (if-let [msg (dom/get-value :chat)]
    (if (> (count msg) 2)
      (do
        (send-msg! (dom/get-value :chat))
        (dom/set-value :chat "")))))

(defn keydown-handler
  [event]
  (let [char-code (.-key event)]
    (if
      (= char-code "Enter")
      (send-chat-message)
      )))

(defn init!
  "Initializes the handlers and websocket"
  []
  (make-web-socket!)
  (event/listen (dom/get-element "chat") :keydown keydown-handler)
  (event/listen (dom/get-element "sendbtn") :click send-chat-message))