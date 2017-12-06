(ns m-venue.map-edit
  (:require [cljs.spec.alpha :as s]
            [m-venue.map-edit-primitives :refer [get-primitive]]
            [m-venue.util :as util]))

(defonce counter (atom 0))
(defonce map-edit-data (atom {}))

(defn reset-map-edit
  []
  (reset! counter 0)
  (reset! map-edit-data {}))

(defmulti get-edit-map
          "Gets the edit functions and html needed to change the value"
          (fn [level spec data]
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
  [level spec data]
  (get-primitive (swap! counter inc) spec (spec data)))

(defn- map-reducer
  [required result-map [path function-map]]
  (util/log (str "map-reducer called with result-map: " result-map " path " path " and value " ((:get-value-f function-map))))
  (if result-map
    (if-let [value ((:get-value-f function-map))]
      (if (keyword? path)
        (assoc result-map path value)
        (merge value result-map))
      (if required
        nil
        result-map))))

(defn- get-edit-keys
  [[& {:keys [req opt]}] level data]
  (let [new-level (+ 1 level)
        req-elements (mapv #(vector % (get-edit-map new-level % data)) req)
        opt-elements (mapv #(vector % (get-edit-map new-level % data)) opt)
        id (str "keys-" (swap! counter inc))]
    {:html         [:div {:id id}
                    (map #(:html (second %)) req-elements)
                    (map #(:html (second %)) opt-elements)]
     :init-f       #(do (doseq [element req-elements] (if-let [el-function (:init-f (second element))] (el-function)))
                        (doseq [element opt-elements] (if-let [el-function (:init-f (second element))] (el-function))))
     :validation-f #(doseq [element req-elements] ((:validation-f (second element))))
     :get-value-f  #(reduce (partial map-reducer false) (reduce (partial map-reducer true) {} req-elements) opt-elements)
     }))

(defmethod get-edit-map :keys
  [level spec data]
  (let [new-data (if (> level 0) (spec data) data)]
    (get-edit-keys (rest (s/form spec)) level new-data)))

(defn- ser-merge-part
  [level merge-part data]
  (if
    (keyword? merge-part)
    (get-edit-keys level (rest (s/form merge-part)) data)
    (get-edit-keys level (rest merge-part) data)))

(defn- merge-reducer
  [result-map function-map]
  (if-let [value ((:get-value-f function-map))]
    (merge result-map value)
    result-map))

(defmethod get-edit-map :merge
  [level spec data]
  (let [parts (mapv #(ser-merge-part (+ 1 level) % data) (rest (s/form spec)))
        id (str "merge-" (swap! counter inc))]
    {:html         [:div {:id id} (map #(:html %) parts)]
     :init-f       #(doseq [part parts] (if-let [part-function (:init-f part)] (part-function)))
     :validation-f #(doseq [part parts] ((:validation-f part)))
     :get-value-f  #(reduce merge-reducer {} parts)
     }))

(defn vector-map-reducer
  [map range-id part]
  (assoc map range-id
             {:validation-f (:validation-f part)
              :get-value-f  (:get-value-f part)}))

(defn remove-vector-part
  [id range-id]
  (do
    (util/remove-node (str id "-" range-id))
    (swap! map-edit-data #(update % id dissoc range-id))))

(defn add-to-vector
  [level id spec-type]
  (let [part (get-edit-map level spec-type nil)
        range-id (inc (last (keys (get @map-edit-data id))))]
    (util/set-html [:div.notification {:id (str id "-" range-id)}
                    [:div [:button.delete {:id (str id "-button-" range-id)}]]
                    (:html part)] id false)
    (swap! map-edit-data #(assoc-in % [id range-id] {:validation-f (:validation-f part)
                                                     :get-value-f  (:get-value-f part)}))
    (if-let [part-function (:init-f part)] (part-function))
    (util/on-click-once (str id "-button-" range-id) (fn [] (remove-vector-part id range-id)))))

(defmethod get-edit-map :vector
  [level spec data]
  (let [new-level (+ 1 level)
        spec-form (s/form spec)
        spec-type (second (nth spec-form 2))
        parts (mapv #(get-edit-map new-level spec-type {spec-type %}) (spec data))
        id (str "vector-" (swap! counter inc))
        function-map (reduce-kv vector-map-reducer {} parts)]
    (swap! map-edit-data #(assoc % id function-map))
    {:html         [:div [:div {:id id}
                          (map-indexed #(vector :div.notification {:id (str id "-" %1)}
                                                [:div [:button.delete {:id (str id "-button-" %1)}]] (:html %2)) parts)]
                    [:p.control [:button.button.is-primary {:id (str id "-plus")}
                                 [:span.icon [:i.mdi.mdi-24px.mdi-plus]]]]]
     :init-f       #(do
                      (doseq [part parts] (if-let [part-function (:init-f part)] (part-function)))
                      (doseq [range-id (range (count parts))]
                        (util/on-click-once (str id "-button-" range-id) (fn [] (remove-vector-part id range-id))))
                      (util/on-click (str id "-plus") (fn [] (add-to-vector new-level id spec-type))))
     :validation-f #(doseq [[key part] (get @map-edit-data id)] ((:validation-f part)))
     :get-value-f  #(mapv (fn [[key part]] ((:get-value-f part))) (get @map-edit-data id))
     }))

(defn keys-present
  [is-and part data]
  (if is-and
    (reduce #(and %1 (contains? data %2)) true (rest part))
    (contains? data part)))

(defn get-default
  [current-default is-and id part data]
  (if current-default
    current-default
    (if (keys-present is-and part data)
      id
      nil)))

(defn get-or-function-map
  [id is-and level part data]
  (if is-and
    (let [and-elements (mapv #(vector % (get-edit-map level % data)) (rest part))]
      {:html         [:div {:id id} (map #(:html (second %)) and-elements)]
       :init-f       #(doseq [element and-elements] (if-let [el-function (:init-f (second element))] (el-function)))
       :validation-f #(doseq [element and-elements] ((:validation-f (second element))))
       :get-value-f  #(reduce (partial map-reducer true) {} and-elements)
       })
    (let [or-element (get-edit-map level part data)]
      {:html         [:div {:id id} (:html or-element)]
       :init-f       #((:init-f or-element))
       :validation-f #((:validation-f or-element))
       :get-value-f  #(map-reducer true {} [part or-element])
       })))

(defn or-reducer
  [data parent-id level result id part]
  (let [is-and (coll? part)
        default (get-default (first result) is-and id part data)
        function-map (get-or-function-map (str parent-id "-" id) is-and level part data)]
    [default (conj (second result) function-map)]))

(defn get-or-html
  [index default or-map]
  (if (not (= index default))
    (let [html (:html or-map)
          new-map (assoc (second html) :style "display:none")]
      [(first html) new-map (nth html 2)])
    (:html or-map)
    ))

(defn get-radio
  [id default or-maps-count]
  [:div.control {:id id}
   (for [it (range or-maps-count)]
     (let [checked (if (= it default) "true" nil)]
       [:label.radio [:input {:type "radio" :name id :checked checked :value it} it]]))])

(defmethod get-edit-map :or
  [level spec data]
  (let [new-level (+ 1 level)
        id (str "or-" (swap! counter inc))
        [r-default or-maps] (reduce-kv (partial or-reducer data id new-level) [nil []] (into [] (rest spec)))
        default (if r-default r-default 0)
        radio-id (str id "-radio")
        get-radio-value #(int (util/get-radio-value radio-id))]
    {:html         [:div {:id id} (get-radio radio-id default (count or-maps)) (map-indexed #(get-or-html %1 default %2) or-maps)]
     :init-f       #(do
                      (doseq [element or-maps] (if-let [el-function (:init-f (second element))] (el-function)))
                      (util/log (get-radio-value)))
     :validation-f #((:validation-f (nth or-maps (get-radio-value))))
     :get-value-f  #((:get-value-f (nth or-maps (get-radio-value))))
     }
    ))