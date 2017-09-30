(ns m-venue.chat
  (:require [clojure.browser.dom :as dom]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [m-venue.web-socket :refer [send-msg! subscribe]]
            [m-venue.util :as util]))

(defonce message-counter (atom 0))

(defn receive
  [msg]
  (let [color (cond
                (string/ends-with? msg "[enter!]") "is-warning"
                (string/ends-with? msg "[left!]") "is-danger"
                :else "is-info")
        li-item (. js/document createElement "p")]
    (set! (.-className li-item) (str "notification tile chat-tile " color))
    (set! (.-id li-item) (str "chat-message-" @message-counter))
    (dom/set-text li-item msg)
    (dom/insert-at (util/ensure-element :board) li-item 0)
    (if
      (> @message-counter 4)
      (gdom/removeNode (dom/get-element (str "chat-message-" (- @message-counter 5)))))
    (swap! message-counter inc)))

(defn send-chat-message
  []
  (if-let [msg (dom/get-value :chat)]
    (if (> (count msg) 2)
      (do
        (send-msg! (str "ch-" (dom/get-value :chat)))
        (dom/set-value :chat "")))))

(defn init!
  "Initializes the handlers"
  []
  (util/on-enter :chat send-chat-message)
  (util/on-click :sendbtn send-chat-message)
  (subscribe "ch-" #(receive %)))