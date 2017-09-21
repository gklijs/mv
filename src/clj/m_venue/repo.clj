(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [m-venue.spec]
            [spec-serialize.core :refer [de-ser-vector]]
            [spec-serialize.impl :refer [from-string to-string]]))

(def repo (atom {}))

(defn get-map
  "get map item from repo"
  [key]
  (if-let [string-value (get @repo key)]
    (from-string string-value)))

(defn set-map!
  "validate and add/overwrite item in repo"
  [key spec data]
  (if
    (s/valid? spec data)
    (swap! repo #(assoc % key (to-string spec data)))
    (s/explain-data spec data)))

(defn get-string
  "get string item from repo"
  [key]
  (if-let [string-value (get @repo key)]
    string-value))

(defn set-string!
  "validate and add/overwrite item in repo"
  [key data]
  (if-let [[spec vec-value] (read-string data)]
    (if (s/valid? spec (de-ser-vector spec vec-value))
      (swap! repo #(assoc % key data)))))