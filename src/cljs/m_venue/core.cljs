(ns m-venue.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.browser.dom :as dom])
  (:import goog.History))

(def app (atom nil))

(defn init!
      "Initializes the handlers and websocket"
      []
      (println "init called")
      (println "init called again")
      (dom/set-text (dom/get-element "app2") "It's working")
      (go-loop [seconds 1]
               (<! (timeout 1000))
               (dom/set-text (dom/get-element "app2") (str "waited " seconds " seconds"))
               (recur (inc seconds))))