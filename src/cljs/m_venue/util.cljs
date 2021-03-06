(ns m-venue.util
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [clojure.string :as string]
            [goog.events :as gevents]
            [goog.events.EventType :as EventType]
            [goog.dom :as gdom]
            [goog.dom.classlist :as classlist]
            [goog.dom.dataset :as dataset]
            [goog.html.legacyconversions :as legacy]
            [hiccups.runtime :as hiccupsrt]))

(defn log [x] (js/console.log x))

(defn- lazy-nl-via-item
  ([nl] (lazy-nl-via-item nl 0))
  ([nl n] (when (< n (. nl -length))
            (lazy-seq
              (cons (. nl (item n))
                    (lazy-nl-via-item nl (inc n)))))))

(defn- lazy-nl-via-array-ref
  ([nl] (lazy-nl-via-array-ref nl 0))
  ([nl n] (when (< n (. nl -length))
            (lazy-seq
              (cons (aget nl n)
                    (lazy-nl-via-array-ref nl (inc n)))))))

(defn- lazy-nodelist
  [nl]
  (if (. nl -item)
    (lazy-nl-via-item nl)
    (lazy-nl-via-array-ref nl)))

(extend-type js/HTMLCollection
  ICounted
  (-count [coll] (. coll -length))

  IIndexed
  (-nth
    ([coll n] (. coll (item n)))
    ([coll n not-found] (if (<= (. coll -length) n)
                          not-found
                          (nth coll n))))

  ISeqable
  (-seq [coll] (lazy-nodelist coll)))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(defn ensure-element [x]
  (cond
    (gdom/isElement x) x
    (string? x) (gdom/getElement x)
    (keyword? x) (gdom/getElement (name x))
    (gdom/isNodeLike x) (gdom/getElement x)
    :else (do (log (str "could not convert to element: " x)) nil)))

(defn on-click
  [id f]
  (if-let [e (ensure-element id)]
    (gevents/listen e EventType/CLICK f)
    (print "Could not find element with id " id)))

(defn on-click-target
  [id f]
  (gevents/listen (ensure-element id) EventType/CLICK
                  (fn [evt]
                    (let [target (.-target evt)]
                      (f target)))))

(defn on-click-once [id f] (gevents/listenOnce (ensure-element id) EventType/CLICK f))

(defn on-click-all-by-class
  "set onclick for all with id starting with"
  [class f]
  (let [items (.querySelectorAll js/document (str "." class))]
    (doseq [id items]
      (on-click-target id f))))

(defn on-change
  [id f]
  (gevents/listen (ensure-element id) EventType/CHANGE f))

(defn unlisten-by-key
  [key]
  (gevents/unlistenByKey key))

(defn enter-filter
  [f event]
  (let [char-code (.-key event)]
    (if (= char-code "Enter") (f))))

(defn on-keydown
  [id f]
  (gevents/listen (ensure-element id) EventType/KEYDOWN (fn [evt] (f evt))))

(defn on-enter
  [id f]
  (on-keydown (ensure-element id) (partial enter-filter f)))

(defn toggle-class [id class]
  (if-let [element (ensure-element (ensure-element id))]
    (classlist/toggle element class)))

(defn add-class! [id class]
  (if-let [element (ensure-element (ensure-element id))]
    (classlist/add element class)))

(defn remove-class! [id class]
  (if-let [element (ensure-element (ensure-element id))]
    (classlist/remove element class)))

(defn toggle-visibility [id]
  (let [element (ensure-element id)
        style-display (.-display (.-style element))]
    (if (= "none" style-display)
      (set! (.-display (.-style element)) "")
      (set! (.-display (.-style element)) "none"))))

(defn hide [id]
  (if-let [element (ensure-element id)]
    (set! (.-display (.-style element)) "none")))

(defn show [id]
  (if-let [element (ensure-element id)]
    (set! (.-display (.-style element)) "")))

(defn is-visible? [id]
  (let [element (ensure-element id)
        style-display (.-display (.-style element))]
    (= style-display "")))

(defn node-from-data
  [data]
  (gdom/safeHtmlToNode (legacy/safeHtmlFromString (html data))))

(defn set-html
  ([data] (set-html data nil))
  ([data parent-id] (set-html data parent-id true))
  ([data parent-id remove-childs] (set-html data parent-id remove-childs nil))
  ([data parent-id remove-childs place-at]
   (let [new-node (node-from-data data)
         node-id (.-id new-node)
         current-node (if (nil? node-id) nil (ensure-element node-id))]
     (if current-node
       (gdom/replaceNode new-node current-node)
       (if-let [parent (ensure-element parent-id)]
         (do
           (if remove-childs (gdom/removeChildren parent))
           (if place-at
             (gdom/insertChildAt parent new-node place-at)
             (gdom/append parent new-node)))
         (log (str "could not place html: " data " on parent: " parent-id)))))))

(defn enable
  [id]
  (let [element (ensure-element id)]
    (set! (.-disabled element) false)))

(defn disable
  [id]
  (let [element (ensure-element id)]
    (set! (.-disabled element) true)))

(defn remove-node
  [id]
  (if-let [element (ensure-element id)]
    (gdom/removeNode element)))

(defn get-data
  [id key]
  (if-let [element (ensure-element id)]
    (dataset/get element key)))

(defn get-language
  []
  (.-lang (.-documentElement js/document)))

(defn get-radio-value
  [name]
  (.-value (.querySelector js/document (str "input[name=\"" name "\"]:checked"))))

(defn get-path
  []
  (if-let [path (.-pathname (.-location js/window))]
    (rest (string/split path #"/"))))

(defn for-all-children
  [id el-type function]
  (if-let [element (ensure-element id)]
    (mapv function (gdom/getChildren element el-type))))

(defn move-up
  [id]
  (if-let [node (ensure-element id)]
    (if-let [prev (gdom/getPreviousElementSibling node)]
      (do
        (gdom/removeNode node)
        (gdom/insertSiblingBefore node prev)))))

(defn move-down
  [id]
  (if-let [node (ensure-element id)]
    (if-let [prev (gdom/getNextElementSibling node)]
      (do
        (gdom/removeNode node)
        (gdom/insertSiblingAfter node prev)))))

(defn get-viewport-size
  []
  (let [vs (gdom/getViewportSize)]
    [(.-height vs) (.-width vs)]))