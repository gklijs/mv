(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [m-venue.content-db :as content-db]
            [m-venue.spec]
            [spec-serialize.core :refer [de-ser-vector]]
            [spec-serialize.impl :refer [from-string to-string]]))

(defn set-string!
  "validate and add/overwrite item in repo"
  [key data]
  (if-let [[spec vec-value] (read-string data)]
    (if (s/valid? spec (de-ser-vector spec vec-value))
      (content-db/set-content key data))))

(defn set-map!
  "validate and add/overwrite item in repo"
  [key spec data]
  (if
    (s/valid? spec data)
    (content-db/set-content key (to-string spec data))
    (s/explain-data spec data)))

(defn get-string
  "get string item from repo"
  [key]
  (content-db/get-content key))

(defn get-map
  "get map item from repo"
  [key]
  (if-let [string-value (get-string key)]
    (from-string string-value)))

(defn for-all
  [f-for-each]
  (content-db/for-all #(f-for-each (str %1 "-" %2) %3)))