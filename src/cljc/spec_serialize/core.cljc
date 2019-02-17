(ns spec-serialize.core
  (:require [clojure.spec.alpha :as s]))

(defonce defaults (atom {}))

(defn set-default
  "Adds a default value to be used to deserialize data"
  [keyword value]
  (swap! defaults #(assoc % keyword value)))

(defmulti add-value
          "Adds a value from the data vector, possibly add a default if the value is nil"
          (fn [_ _ _ _ itm]
            (if
              (keyword? itm)
              (let [spec-form (s/form itm)]
                (cond
                  (symbol? spec-form) :keyword
                  (= (first spec-form) `s/keys) :keys
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `vector?)) :vector
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `map?)) :map
                  :else :keyword))
              :or)))

(defmethod add-value :keyword
  [use-defaults data-vector map idx itm]
  (let [data-part (nth data-vector idx nil)]
    (if (nil? data-part)
      (if use-defaults (assoc map itm (get @defaults itm)) map)
      (assoc map itm data-part))))

(declare de-ser-keys)

(defmethod add-value :keys
  [use-defaults data-vector map idx itm]
  (let [data-part (nth data-vector idx nil)]
    (if (nil? data-part)
      (if use-defaults (assoc map itm (get @defaults itm)) map)
      (let [maybe-empty (de-ser-keys (rest (s/form itm)) data-part)]
        (if (empty? maybe-empty)
          map
          (assoc map itm maybe-empty))))))

(defmethod add-value :vector
  [use-defaults data-vector m idx itm]
  (let [data-part (nth data-vector idx nil)
        [_ _ [_ spec-type]] (s/form itm)
        part-form (s/form spec-type)]
    (if (nil? data-part)
      (if use-defaults (assoc m itm (get @defaults itm)) m)
      (if (and (coll? part-form) (= `s/keys (first part-form)))
        (assoc m itm (mapv #(de-ser-keys (rest part-form) %) data-part))
        (assoc m itm data-part)))))

(defmethod add-value :map
  [use-defaults data-vector m idx itm]
  (let [data-part (nth data-vector idx nil)
        [_ _ [_ key-type value-type]] (s/form itm)
        key-form (if (keyword? key-type) (s/form key-type))
        value-form (if (keyword? value-type) (s/form value-type))
        key-f (if
                (and (coll? key-form) (= `s/keys (first key-form)))
                #(de-ser-keys (rest key-form) %)
                #(identity %))
        value-f (if
                  (and (coll? value-form) (= `s/keys (first value-form)))
                  #(de-ser-keys (rest value-form) %)
                  #(identity %))]
    (if (nil? data-part)
      (if use-defaults (assoc m itm (get @defaults itm)) m)
      (assoc m itm (into {} (map (fn [[k v]] [(key-f k) (value-f v)]) data-part))))))

(defmethod add-value :or
  [use-defaults data-vector map idx itm]
  (if-let [operator-type (name (first itm))]
    (cond
      (= operator-type "or")
      (let [data-part (nth data-vector idx nil)]
        (if (nil? data-part)
          (if use-defaults (add-value true nil map 0 (second itm)) map)
          (add-value use-defaults (rest data-part) map 0 (nth itm (inc (first data-part))))))
      (= operator-type "and")
      (if (nil? data-vector)
        (if use-defaults (reduce-kv (partial add-value true nil) map (vec (rest itm))) map)
        (reduce-kv (partial add-value use-defaults data-vector) map (vec (rest itm))))
      :else map)
    map))

(defn- de-ser-keys
  "Deserialize data from the vector back to a map"
  [[& {:keys [req opt]}] vec]
  (let [req-values (reduce-kv (partial add-value true (first vec)) {} req)
        all-values (reduce-kv (partial add-value false (second vec)) req-values opt)]
    all-values))

(defn- de-ser-merge
  "Handles serialize on a merge object part."
  [data-vector map idx itm]
  (let [add-map (if
                  (keyword? itm)
                  (de-ser-keys (rest (s/form itm)) (nth data-vector idx))
                  (de-ser-keys (rest itm) (nth data-vector idx)))]
    (merge map add-map)))

(defn de-ser-vector
  "Deserialize a vector back into a map, will fill in defaults where needed and available for required namespaces."
  [spec vec-values]
  (let [spec-form (s/form spec)
        spec-type (first spec-form)]
    (cond
      (= spec-type `s/keys) (de-ser-keys (rest spec-form) vec-values)
      (= spec-type `s/merge) (reduce-kv (partial de-ser-merge vec-values) {} (vec (rest spec-form)))
      :else nil)))

(defmulti ser-key-part-or-reducer
          "Handles serialize on or-ed key part"
          (fn [_ _ keyword-or-list] (keyword? keyword-or-list)))

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
    (and (= (first keyword-or-list) `and) (contains? data (second keyword-or-list)))
    (vec (concat coll (mapv #(get data %) (rest keyword-or-list))))
    :else
    (mapv inc coll)))

(declare ser-map)
(declare ser-keys)

(defmulti ser-value
          "Serialize some value, removing the keys, which we can add later on, because we know the spec"
          (fn [spec _]
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
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `map?)) :map
                  :else :keyword))
              :or)))

(defmethod ser-value :keyword
  [spec data]
  (get data spec))

(defmethod ser-value :keys
  [spec data]
  (when-let [key-data (get data spec)]
    (ser-map spec key-data)))

(defmethod ser-value :vector
  [spec data]
  (when-let [vector-data (get data spec)]
    (let [[_ _ [_ spec-type]] (s/form spec)
          part-form (s/form spec-type)]
      (if (and (coll? part-form) (= `s/keys (first part-form)))
        (mapv #(ser-keys (rest part-form) %) vector-data)
        vector-data))))

(defmethod ser-value :map
  [spec data]
  (when-let [map-data (get data spec)]
    (let [[_ _ [_ key-type value-type]] (s/form spec)
          key-form (if (keyword? key-type) (s/form key-type))
          value-form (if (keyword? value-type) (s/form value-type))
          key-f (if
                  (and (coll? key-form) (= `s/keys (first key-form)))
                  #(ser-keys (rest key-form) %)
                  #(identity %))
          value-f (if
                    (and (coll? value-form) (= `s/keys (first value-form)))
                    #(ser-keys (rest value-form) %)
                    #(identity %))]
      (into {} (map (fn [[k v]] [(key-f k) (value-f v)]) map-data)))))

(defmethod ser-value :or
  [spec data]
  (let [reduce-result (reduce (partial ser-key-part-or-reducer data) [0] (rest spec))]
    (if (> (count reduce-result) 1) reduce-result [0])))

(defn- ser-keys
  "Handles serialize on a keys object."
  [[& {:keys [req opt]}] data]
  (let [req-values (mapv #(ser-value % data) req)
        opt-values (mapv #(ser-value % data) opt)]
    [req-values opt-values]))

(defn- ser-merge-part
  "Handles serialize on a merge object part."
  [merge-part data]
  (if
    (keyword? merge-part)
    (ser-keys (rest (s/form merge-part)) data)
    (ser-keys (rest merge-part) data)))

(defn ser-map
  "Serializes a map to provide a shorter but reversible data structure, defaults will only by checked on deserialize to keep the data small"
  [spec data]
  (let [spec-form (s/form spec)
        spec-type (first spec-form)]
    (cond
      (= spec-type `s/keys) (ser-keys (rest spec-form) data)
      (= spec-type `s/merge) (mapv #(ser-merge-part % data) (rest spec-form))
      :else data)))
