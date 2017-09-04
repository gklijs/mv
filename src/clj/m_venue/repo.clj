(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [m-venue.spec :refer :all]
            [spec-serialize.impl :as tf]))

(def repo (atom {}))

(defn get-spec
  "get spec based on key"
  [key]
  (let [type (subs key 3 5)]
    (cond
      (= type "gd") :m-venue.spec/gen-doc
      :else nil)))

(defn get-map
  "get item from repo"
  [key]
  (if-let [spec (get-spec key)]
    (if-let [bytes (get @repo key)]
      (tf/de-ser-bytes spec bytes))))

(defn set-map
  "validate and add/overwrite item in repo"
  [key data]
  (if-let [spec (get-spec key)]
    (if
      (s/valid? spec data)
      (swap! repo #(assoc % key (tf/ser-to-bytes spec data)))
      (s/explain-data spec data))))
