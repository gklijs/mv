(ns m-venue.mc
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [clojure.browser.dom :as dom]
            [clojure.string :as string]
            [cljs.reader]
            [goog.dom :as gdom]
            [goog.html.legacyconversions :as legacy]
            [hiccups.runtime :as hiccupsrt]
            [m-venue.websocket :refer [send-msg! subscribe]]
            [m-venue.templates :as templates]
            [spec-serialize.impl :as tf]))

(defonce counter (atom 1))

(defn set-gen-doc
  [msg]
  (let [[spec map-data] (tf/from-string (subs msg 3))
        parent (if (even? @counter) (dom/get-element :child-tiles-left) (dom/get-element :child-tiles-right))]
    (dom/append parent (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html (templates/tile map-data (str "gen-" @counter))))))
    (swap! counter inc)))

(defn init!
  "Initializes the handlers"
  []
  (subscribe (fn [msg] (string/starts-with? msg "mc-")) (fn [msg] (set-gen-doc msg))))

