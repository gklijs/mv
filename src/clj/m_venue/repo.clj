(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [m-venue.spec :refer :all]
            [spec-serialize.impl :as spec-s]))

(def repo (atom {}))

(defn get-map
  "get item from repo"
  [key]
    (if-let [string-value (get @repo key)]
      (spec-s/from-string string-value)))

(defn set-map
  "validate and add/overwrite item in repo"
  [key spec data]
    (if
      (s/valid? spec data)
      (swap! repo #(assoc % key (spec-s/to-string spec data)))
      (s/explain-data spec data)))
