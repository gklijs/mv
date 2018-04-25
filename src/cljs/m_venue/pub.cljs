(ns m-venue.pub
  (:require [m-venue.basic :as basic]
            [m-venue.img-doc :as img-doc]))

(defn init!
  "Initializes the handlers and websocket"
  []
  (basic/init!)
  (img-doc/init!))