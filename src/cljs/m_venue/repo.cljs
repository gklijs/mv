(ns m-venue.repo
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [clojure.string :as string]
            [m-venue.web-socket :refer [send-msg! subscribe]]
            [m-venue.spec]
            [spec-serialize.impl :refer [from-string]]
            [m-venue.util :as util]))

(defonce render-functions (atom {}))

(defn set-renderer!
  [regex render-function]
  (swap! render-functions #(assoc % regex render-function)))

(defn render-loop
  [key val]
  (doseq [[regex render-function] @render-functions]
    (if
      (re-matches regex key)
      (render-function (from-string val)))))

(defn not-local-only
  "utility to use when not to get/set data on the server"
  [key]
  (not (string/starts-with? key "l-")))

(defn set-item!
  "Send the data to the server repo and set `key' in browser's localStorage to `val`."
  [key val]
  (if (not-local-only key)
    (send-msg! (str "set" key val)))
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  [key]
  (.getItem (.-localStorage js/window) key))

(defn execute-with-map
  "Returns value of `key' from browser's localStorage if accessible, otherwise tries to get it from remote"
  ([key function] (execute-with-map key function false))
  ([key function get-called] (execute-with-map key function get-called 0))
  ([key function get-called loops]
   (if-let [val (.getItem (.-localStorage js/window) key)]
     (function (from-string val))
     (if (not-local-only key)
       (do
         (if (false? get-called) (send-msg! (str "get" key)))
         (if (< loops 10) (js/setTimeout #(execute-with-map key function false (inc loops)) 100)))))))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

(defn clear-local-storage!
  "Remove the browser's localStorage value for the given `key`"
  []
  (.clear (.-localStorage js/window)))

(defn receive
  [msg]
  (if-let [[key val] (string/split msg #":" 2)]
    (if
      (not (= val (.getItem (.-localStorage js/window) key)))
      (do
        (render-loop key val)
        (.setItem (.-localStorage js/window) key val)))))

(defn init!
  "Initializes the handlers"
  []
  (subscribe "get" #(receive %))
  (event/listen (dom/get-element :clear-storage-button) :click clear-local-storage!))


