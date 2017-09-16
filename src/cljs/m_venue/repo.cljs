(ns m-venue.repo
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [cljs.core.async :refer [<! timeout]]
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
  "Returns value of `key' from browser's localStorage if accessible, otherwise tries to get it from remote"
  [key]
  (if-let [val (.getItem (.-localStorage js/window) key)]
    (from-string val)
    (if (not-local-only key)
      (do
        (send-msg! (str "get" key))
        nil))))

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


