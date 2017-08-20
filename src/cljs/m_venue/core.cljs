(ns m-venue.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]])
  (:import goog.History))

(def app2 (atom nil))

(defn init!
  "Initializes the handlers and websocket"
  []
    (println "init called")
    (go
      (<! (timeout 3000))
      (reset! app2 (new js/Vue (js-obj "el" "#app2" "data" (js-obj "message" "initialized component")))))
    (go
      (<! (timeout 6000))
      (aset @app2 "message" "It's still working")))