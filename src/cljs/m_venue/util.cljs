(ns m-venue.util
  (:import [goog.dom query]
           [goog.editor.Command]
           [goog.editor.Field]
           [goog.events.EventType])
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [goog.events :as gevents]
            [goog.dom :as gdom]
            [goog.dom.classlist :as classlist]
            [goog.html.legacyconversions :as legacy]
            [hiccups.runtime :as hiccupsrt]))

(defn log [x] (js/console.log x))

(defn ensure-element [x]
  (cond
    (gdom/isElement x) x
    (string? x) (gdom/getElement x)
    (keyword? x) (gdom/getElement (name x))
    (gdom/isNodeLike x) (gdom/getElement x)
    :else (do (log (str "could not convert to element: " x)) nil)))

(defn on-click [id f] (gevents/listen (ensure-element id) goog.events.EventType.CLICK f))

(defn on-click-target
  [id f]
  (gevents/listen (ensure-element id) goog.events.EventType.CLICK
                  (fn [evt]
                    (let [target (.-target evt)]
                      (f target)))))

(defn on-change
  [id f]
  (gevents/listen (ensure-element id) goog.events.EventType.CHANGE f))

(defn enter-filter
  [f event]
  (let [char-code (.-key event)]
    (if (= char-code "Enter") (f))))

(defn on-keydown
  [id f]
  (gevents/listen (ensure-element id) goog.events.EventType.KEYDOWN (fn [evt] (f evt))))

(defn on-enter
  [id f]
  (on-keydown (ensure-element id) (partial enter-filter f)))

(defn toggle-class [id class]
  (if-let [element (ensure-element (ensure-element id))]
    (classlist/toggle element class)))

(defn toggle-visibility [id]
  (let [element (ensure-element id)
        style-display (.-display (.-style element))]
    (if (= "none" style-display)
      (set! (.-display (.-style element)) "")
      (set! (.-display (.-style element)) "none"))))

(defn set-placeholder
  [id value]
  (if-let [element (ensure-element id)]
    (set! (.-placeholder element) value)))

(defn node-from-data
  [data]
  (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html data))))

(defn set-html
  ([data] (set-html data nil))
  ([data parent-id] (set-html data parent-id true))
  ([data parent-id remove-childs]
   (let [new-node (node-from-data data)
         node-id (.-id new-node)
         current-node (if (nil? node-id) nil (ensure-element node-id))]
     (if current-node
       (gdom/replaceNode new-node current-node)
       (if-let [parent (ensure-element parent-id)]
         (do
           (if remove-childs (gdom/removeChildren parent))
           (gdom/append parent new-node))
         (log (str "could not place html: " data " on parent: " parent-id)))))))