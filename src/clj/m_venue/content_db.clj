(ns m-venue.content-db
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string])
  (:import (org.h2.mvstore MVStore)))

(defonce content-store (MVStore/open "content.db"))
(defonce types ["i" "p" "n"])

(defn get-content-map
  [type]
  (.openMap content-store type))

(defn set-content
  [key data]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (let [content-map (get-content-map type)]
      (.put content-map specifier data))
    (log/debug "could not set data: " data " because invalid key: " key)))

(defn get-content
  [key]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (let [content-map (get-content-map type)]
      (.get content-map specifier))
    (log/debug "could not get data because invalid key: " key)))

(defn remove-key
  [key]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (let [content-map (get-content-map type)]
      (.remove content-map specifier))
    (log/debug "could not get data because invalid key: " key)))

(defn close
  []
  (.close content-store))

(defn for-all
  [f-for-all]
  (doseq [type types]
    (doseq [[key data] (get-content-map type)]
      (log/debug type key data)
      (f-for-all type key data))))
