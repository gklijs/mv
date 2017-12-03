(ns m-venue.repo
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [m-venue.web-socket :refer [send-msg! subscribe]]
            [m-venue.util :as util]
            [spec-serialize.impl :refer [from-string to-string]]))

(defonce render-functions (atom {}))
(defonce delayed-functions (atom []))

(defn set-renderer!
  [regex render-function]
  (swap! render-functions #(assoc % regex render-function)))

(defn set-delayed!
  [key delayed-function]
  (swap! delayed-functions #(conj % [key delayed-function])))

(defn render-loop
  [key val]
  (doseq [[regex render-function] @render-functions]
    (if
      (re-matches regex key)
      (render-function (from-string val)))))

(defn delayed-loop
  [key val]
  (let [split-on-regex (group-by #(= key (first %)) @delayed-functions)]
    (if-let [matches (get split-on-regex true)]
      (do
        (doseq [match matches] ((second match) (from-string val)))
        (reset! delayed-functions
                (if-let [non-matches (get split-on-regex false)]
                  non-matches
                  []))))))

(defn set-map!
  "validate and add/overwrite item in repo"
  [key spec data]
  (if
    (s/valid? spec data)
    (let [val (to-string spec data)]
      (do
        (send-msg! (str "set" key ":" val))
        (.setItem (.-localStorage js/window) key val)))
    (util/log (str (s/explain-data spec data)))))

(defn get-map
  [key]
  (if-let [val (.getItem (.-localStorage js/window) key)]
    (from-string val)))

(defn execute-with-map
  "Returns value of `key' from browser's localStorage if accessible, otherwise tries to get it from remote"
  [key function]
  (if-let [val (.getItem (.-localStorage js/window) key)]
    (function (from-string val))
    (do
      (set-delayed! key function)
      (send-msg! (str "get" key)))))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

(defn clear-local-storage!
  "Remove all the browser's localStorage"
  []
  (.clear (.-localStorage js/window)))

(defn get-total-size
  []
  (.-length (.-localStorage js/window)))

(defn receive
  [msg]
  (if-let [[key val] (string/split msg #":" 2)]
    (if
      (not (= val (.getItem (.-localStorage js/window) key)))
      (do
        (render-loop key val)
        (delayed-loop key val)
        (.setItem (.-localStorage js/window) key val)))))

(defn init!
  "Initializes the handlers"
  []
  (subscribe "set" #(receive %))
  (send-msg! (str "ls-" (get-total-size)))
  (util/on-click (util/ensure-element :clear-storage-button) clear-local-storage!))


