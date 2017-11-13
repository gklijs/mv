(ns m-venue.map-edit
  (:require [cljs.spec.alpha :as s]
            [m-venue.map-edit-primitives :refer [get-primitive]]))

(defonce counter (atom 0))

(defmulti ser-key-part-or-reducer
          "Handles serialize on or-ed key part"
          (fn [data coll keyword-or-list] (keyword? keyword-or-list)))

(defmethod ser-key-part-or-reducer true
  [data coll keyword-or-list]
  (cond
    (> (count coll) 1) coll
    (contains? data keyword-or-list) (conj coll (get data keyword-or-list))
    :else
    (mapv inc coll)))

(defmethod ser-key-part-or-reducer false
  [data coll keyword-or-list]
  (cond
    (> (count coll) 1)
    coll
    (and (= (first keyword-or-list) `cljs.core/and) (contains? data (second keyword-or-list)))
    (vec (concat coll (mapv #(get data %) (rest keyword-or-list))))
    :else
    (mapv inc coll)))

(declare ser-map)

(defmulti add-edit
          "Gets the edit functions and html needed to change the value"
          (fn [spec data]
            (if
              (keyword? spec)
              (let [spec-form (s/form spec)]
                (cond
                  (symbol? spec-form) :keyword
                  (= (first spec-form) `s/keys) :keys
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `vector?)) :vector
                  :else :keyword))
              :or)))

(defmethod add-edit :keyword
  [spec data]
  (get-primitive (swap! counter inc) (s/form spec) (get data spec)))

(defmethod add-edit :keys
  [spec data]
    (ser-map spec data))

(defmethod add-edit :vector
  [spec data]
  (if-let [vector-data (get data spec)]
    (let [spec-form (s/form spec)
          spec-type (second (nth spec-form 2))
          element-maps(mapv #(ser-map spec-type %) vector-data)]
      {:html [:div (mapv #(:html %) element-maps)]
       :init-f (reduce (fn [k m] ) [] element-maps)})
    nil))

(defmethod add-edit :or
  [spec data]
  (let [reduce-result (reduce (partial ser-key-part-or-reducer data) [0] (rest spec))]
    (if (> (count reduce-result) 1) reduce-result [0])))

(defn- add-edit-keys
  [[& {:keys [req opt]}] data]
  (let [req-values (mapv #(add-edit % data) req)
        opt-values (mapv #(add-edit % data) opt)]
    (into req-values opt-values)))

(defn- ser-merge-part
  [merge-part data]
  (if
    (keyword? merge-part)
    (add-edit-keys (rest (s/form merge-part)) data)
    (add-edit-keys (rest merge-part) data)))

(defn ser-map
  "Returns data needed to edit and safe a map, splitting it into separate parts witch can be combined to one map"
  [spec data ]
   (let [spec-form (s/form spec)
         spec-type (first spec-form)]
     (cond
       (= spec-type `s/keys) (add-edit-keys (rest spec-form) data)
       (= spec-type `s/merge) (mapv #(ser-merge-part % data) (rest spec-form))
       :else nil)))
