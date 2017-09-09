(ns m-venue.editable
  (:require [m-venue.chat :as chat]
            [m-venue.image-sender :as image-sender]
            [m-venue.mc :as mc]
            [m-venue.web-socket :as web-socket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (web-socket/init! "/editable")
  (chat/init!)
  (image-sender/init!)
  (mc/init!))