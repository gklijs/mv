(ns m-venue.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.browser.dom :as dom]
            [m-venue.chat :as chat]
            [m-venue.mc :as mc]
            [m-venue.websocket :as websocket]
            [m-venue.spec]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (websocket/init!)
  (chat/init!)
  (mc/init!)
  (dom/set-text :app2 "It's working")
  (go-loop [seconds 1]
           (<! (timeout 1000))
           (dom/set-text :app2 (str "waited " seconds " seconds"))
           (recur (inc seconds))))