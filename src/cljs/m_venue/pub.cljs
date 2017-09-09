(ns m-venue.pub
  (:require [m-venue.chat :as chat]
            [m-venue.mc :as mc]
            [m-venue.websocket :as websocket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (websocket/init! "/public")
  (chat/init!)
  (mc/init!))