(ns m-venue.pub
  (:require [m-venue.chat :as chat]
            [m-venue.mc :as mc]
            [m-venue.web-socket :as web-socket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (web-socket/init! "/public")
  (chat/init!)
  (mc/init!))