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

(defn set-placeholder
  [id value]
  (if-let [element (dom/ensure-element id)]
    (set! (.-placeholder element) value)))

(defn set-html
  ([data] (set-html data nil))
  ([data parent-id] (set-html data parent-id true))
  ([data parent-id remove-childs]
   (let [new-node (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html data)))
         node-id (.-id new-node)
         current-node (if (nil? node-id) nil (dom/get-element node-id))]
     (if current-node
       (dom/replace-node current-node new-node)
       (if-let [parent (dom/ensure-element parent-id)]
         (do
           (if remove-childs (gdom/removeChildren parent))
           (dom/append parent new-node))
         (log (str "could not place html: " data " on parent: " parent-id)))))))