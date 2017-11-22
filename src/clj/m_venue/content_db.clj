(ns m-venue.content-db
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string])
  (:import (org.h2.mvstore MVStore)))

(defonce content-store (MVStore/open "content.db"))

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
