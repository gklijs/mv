(ns m-venue.mc
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [goog.html.legacyconversions :as legacy]
            [hiccups.runtime :as hiccupsrt]
            [m-venue.web-socket :refer [send-msg! subscribe]]
            [m-venue.templates :as templates]
            [spec-serialize.impl :as tf]))

(defonce counter (atom 1))

(defn set-gen-doc
  [msg]
  (let [[spec map-data] (tf/from-string msg)
        parent (if (even? @counter) (dom/get-element :child-tiles-left) (dom/get-element :child-tiles-right))]
    (dom/append parent (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html (templates/tile map-data (str "gen-" @counter))))))
    (swap! counter inc)))

(defn init!
  "Initializes the handlers"
  []
  (subscribe "mc-" #(set-gen-doc %)))

