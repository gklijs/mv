(ns m-venue.editable
  (:require [m-venue.chat :as chat]
            [m-venue.mc :as mc]
            [m-venue.websocket :as websocket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (websocket/init! "/editable")
  (chat/init!)
  (mc/init!))