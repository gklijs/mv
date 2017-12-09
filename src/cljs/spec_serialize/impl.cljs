(ns spec-serialize.impl
  (:require [cljs.reader :as reader]
            [cljs.spec.alpha :as s]
            [spec-serialize.core :as core]))

(defn to-string
  "Serialize a map to string, will include the spec used in the value"
  [spec data]
  (if-let [vec-values (core/ser-map spec data)]
    (str [spec vec-values])
    nil))

(defn from-string
  "Deserialize string created with the to-string method"
  [string-value]
  (if (string? string-value)
    (if-let [[spec vec-value] (reader/read-string string-value)]
      [spec (core/de-ser-vector spec vec-value)])))
