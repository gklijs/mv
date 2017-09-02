(ns m-venue.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.browser.dom :as dom]
            [m-venue.chat :as chat])
  (:import goog.History))

(defn init!
      "Initializes the handlers and websocket"
      []
      (chat/init!)
      (dom/set-text :app2 "It's working")
      (go-loop [seconds 1]
               (<! (timeout 1000))
               (dom/set-text :app2 (str "waited " seconds " seconds"))
               (recur (inc seconds))))