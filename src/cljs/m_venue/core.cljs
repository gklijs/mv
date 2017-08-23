(ns m-venue.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]])
  (:use [jayq.core :only [$ css html text]])
  (:import goog.History))

(def app (atom nil))

(defn init!
      "Initializes the handlers and websocket"
      []
      (println "init called")
      (swap! app ($ "#app2"))
      (println @app)
      (text @app "It's working")
      (go-loop [seconds 1]
               (<! (timeout 1000))
               (text @app (str "waited " seconds " seconds"))
               (recur (inc seconds))))