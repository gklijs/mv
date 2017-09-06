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

(defn set-gen-doc
  [msg]
  (let [[spec map-data] (tf/from-string (subs msg 3))]
    (dom/remove-children :blabla)
    (dom/append (dom/get-element :blabla)(gdom/safeHtmlToNode (legacy/safeHtmlFromString (html (templates/gd-content map-data)))))))

(defn init!
  "Initializes the handlers"
  []
  (subscribe (fn [msg] (string/starts-with? msg "mc-")) (fn [msg] (set-gen-doc msg))))

