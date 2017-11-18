(ns m-venue.map-edit
  (:require [cljs.spec.alpha :as s]
            [m-venue.map-edit-primitives :refer [get-primitive]]
            [m-venue.util :as util]))

(defonce counter (atom 0))
(defonce map-edit-data (atom {}))

(defn reset-map-edit-data
  []
  (reset! map-edit-data {}))

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

(defmulti get-edit-map
          "Gets the edit functions and html needed to change the value"
          (fn [spec data]
            (if
              (keyword? spec)
              (let [spec-form (s/form spec)]
                (cond
                  (symbol? spec-form) :keyword
                  (= (first spec-form) `s/keys) :keys
                  (= (first spec-form) `s/merge) :merge
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `vector?)) :vector
                  :else :keyword))
              :or)))

(defmethod get-edit-map :keyword
  [spec data]
  (get-primitive (swap! counter inc) spec data))

(defn- map-reducer
  [[path function-map] result-map]
  (if-let [value ((:get-value-f function-map))]
    (assoc result-map path value)
    result-map))

(defn- get-edit-keys
  [[& {:keys [req opt]}] data]
  (let [req-elements (mapv #(vector % (get-edit-map % (get data %))) req)
        opt-elements (mapv #(vector % (get-edit-map % (get data %))) opt)
        id (str "keys-" (swap! counter inc))]
    {:html   [:div {:id id}
              (map #(:html (second %)) req-elements)
              (map #(:html (second %)) opt-elements)]
     :init-f #(do (doseq [element req-elements] (if-let [el-function (:init-f  (second element))] (el-function)))
                  (doseq [element opt-elements] (if-let [el-function (:init-f (second element))] (el-function))))
     :validation-f #(doseq [element req-elements] ((:validation-f (second element))))
     :get-value-f #(reduce map-reducer (into req-elements opt-elements))
     }))

(defmethod get-edit-map :keys
  [spec data]
  (get-edit-keys (rest (s/form spec)) data))

(defn- ser-merge-part
  [merge-part data]
  (if
    (keyword? merge-part)
    (get-edit-keys (rest (s/form merge-part)) data)
    (get-edit-keys (rest merge-part) data)))

(defn- merge-reducer
  [function-map result-map]
  (if-let [value ((:get-value-f function-map))]
    (merge result-map value)
    result-map))

(defmethod get-edit-map :merge
  [spec data]
  (let [parts (mapv #(ser-merge-part % data) (rest (s/form spec)))
        id (str "merge-" (swap! counter inc))]
    {:html [:div {:id id} (map #(:html %) parts)]
     :init-f #(doseq [part parts] (if-let [part-function (:init-f part)] (part-function)))
     :validation-f #(doseq [part parts] ((:validation-f part)))
     :get-value-f #(reduce merge-reducer parts)
     }))

(defn remove-vector-part
  [id range-id]
  (do
    (util/remove-node (str id "-" range-id))
    (util/log (str "still need to remove validation and get for: " range-id))))

(defmethod get-edit-map :vector
  [spec data]
    (let [spec-form (s/form spec)
          spec-type (second (nth spec-form 2))
          parts (mapv #(get-edit-map spec-type %) data)
          id (str "vector-" (swap! counter inc))]
      {:html   [:div {:id id} (map-indexed #(vector :div.notification {:id (str id "-" %1)}
                                                    [:div [:button.delete {:id (str id "-button-" %1)}]] (:html %2)) parts)]
       :init-f #(do
                  (doseq [part parts] (if-let [part-function (:init-f part)] (part-function)))
                  (doseq [range-id (range (count parts))]
                    (util/on-click-once (str id "-button-" range-id) (fn [] (remove-vector-part id range-id)))))
       :validation-f #(doseq [part parts] ((:validation-f part)))
       :get-value-f #(mapv (fn [part] ((:get-value-f part))) parts)
       }))

(defmethod get-edit-map :or
  [spec data]
  (let [reduce-result (reduce (partial ser-key-part-or-reducer data) [0] (rest spec))]
    (if (> (count reduce-result) 1) reduce-result [0])))
