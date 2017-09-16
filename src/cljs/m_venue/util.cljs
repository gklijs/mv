(ns m-venue.util
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [hiccups.runtime :as hiccupsrt]
            [goog.dom :as gdom]
            [goog.html.legacyconversions :as legacy]))

(defn log
  [x]
  (js/console.log x))

(defn toggle-class [id toggled-class]
  (let [element (dom/ensure-element id)
        el-classList (.-classList (dom/ensure-element element))]
    (if (.contains el-classList toggled-class)
      (.remove el-classList toggled-class)
      (.add el-classList toggled-class))))

(defn toggle-visibility [id]
  (let [element (dom/ensure-element id)
        style-display (.-display (.-style element))]
    (if (= "none" style-display)
      (set! (.-display (.-style element)) "")
      (set! (.-display (.-style element)) "none"))))

(defn set-html
  [parent-id data]
  (let [new-node (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html data)))
        node-id (.-id new-node)
        current-node (if (nil? node-id) nil (dom/get-element node-id))]
    (if current-node
      (dom/replace-node current-node new-node)
      (dom/append (dom/ensure-element parent-id) new-node))))