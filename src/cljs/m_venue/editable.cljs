(ns m-venue.editable
  (:require [m-venue.basic :as basic]
            [m-venue.img-doc :as img-doc]
            [m-venue.image-sender :as image-sender]
            [m-venue.image-selection :as image-selection]
            [m-venue.content-edit :as content-edit]
            [m-venue.repo :as repo]
            [m-venue.web-socket :as web-socket]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (basic/init!)
  (img-doc/init!)
  (web-socket/init! "/editable")
  (repo/init!)
  (image-sender/init!)
  (image-selection/init!)
  (content-edit/init!))