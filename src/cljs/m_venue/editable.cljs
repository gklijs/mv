(ns m-venue.editable
  (:require [m-venue.chat :as chat]
            [m-venue.image-sender :as image-sender]
            [m-venue.image-selection :as image-selection]
            [m-venue.content-edit :as content-edit]
            [m-venue.repo :as repo]
            [m-venue.web-socket :as web-socket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (web-socket/init! "/editable")
  (repo/init!)
  (chat/init!)
  (image-sender/init!)
  (image-selection/init!)
  (content-edit/init!))