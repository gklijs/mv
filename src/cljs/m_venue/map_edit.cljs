(ns m-venue.map-edit
  (:require [cljs.spec.alpha :as s]
            [m-venue.constants :refer [flags-map]]
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
          (fn [_ spec _]
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
                  (and (= (first spec-form) `s/and)
                       (coll? (second spec-form))
                       (> (count (second spec-form)) 1)
                       (= (second (second spec-form)) `map?)
                       (= :m-venue.spec/language (second (nth spec-form 2)))) :language-map
                  :else :keyword))
              :or)))

(defmethod get-edit-map :keyword
  [_ spec data]
  (get-primitive (swap! counter inc) spec (spec data)))

(defn- map-reducer
  [required result-map [path function-map]]
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

(defn remove-part
  [id range-id]
  (do
    (util/remove-node (str id "-" range-id))))

(defn get-child-id-list
  [parent-id]
  (util/for-all-children parent-id "div" #(int (util/get-data % "id"))))

(defn vector-map-reducer
  [map range-id part]
  (assoc map range-id
             {:validation-f (:validation-f part)
              :get-value-f  (:get-value-f part)}))

(defn get-vector-html
  [id range-id part-html]
  [:div.notification {:id (str id "-" range-id)}
   [:div [:button.delete {:id (str id "-button-remove-" range-id)}]]
   [:div.field.is-grouped.is-grouped-multiline.is-pulled-right
    [:p.control [:button.button.is-primary {:id (str id "-button-up-" range-id)}
                 [:span.icon [:i.mdi.mdi-24px.mdi-arrow-up-thick]]]]
    [:p.control [:button.button.is-primary {:id (str id "-button-down-" range-id)}
                 [:span.icon [:i.mdi.mdi-24px.mdi-arrow-down-thick]]]]]
   part-html])

(defn add-to-vector
  [level id spec-type first]
  (let [part (get-edit-map level spec-type nil)
        range-id (inc (last (keys (get @map-edit-data id))))]
    (util/set-html (get-vector-html id range-id (:html part)) id false (if first 0 nil))
    (swap! map-edit-data #(assoc-in % [id range-id] {:validation-f (:validation-f part)
                                                     :get-value-f  (:get-value-f part)}))
    (if-let [part-function (:init-f part)] (part-function))
    (util/on-click-once (str id "-button-remove-" range-id) (fn [] (remove-part id range-id)))
    (util/on-click-once (str id "-button-up-" range-id) (fn [] (util/move-up (str id "-" range-id))))
    (util/on-click-once (str id "-button-down-" range-id) (fn [] (util/move-down (str id "-" range-id))))))

(defmethod get-edit-map :vector
  [level spec data]
  (let [new-level (+ 1 level)
        spec-form (s/form spec)
        spec-type (second (nth spec-form 2))
        parts (mapv #(get-edit-map new-level spec-type {spec-type %}) (spec data))
        id (str "vector-" (swap! counter inc))
        function-map (reduce-kv vector-map-reducer {} parts)]
    (swap! map-edit-data #(assoc % id function-map))
    {:html         [:div
                    [:p.control [:button.button.is-primary {:id (str id "-plus-above")}
                                 [:span.icon [:i.mdi.mdi-24px.mdi-plus]]]]
                    [:div {:id id}
                     (map-indexed #(get-vector-html id %1 (:html %2)) parts)]
                    [:p.control [:button.button.is-primary {:id (str id "-plus-under")}
                                 [:span.icon [:i.mdi.mdi-24px.mdi-plus]]]]]
     :init-f       #(do
                      (doseq [part parts] (if-let [part-function (:init-f part)] (part-function)))
                      (doseq [range-id (range (count parts))]
                        (util/on-click-once (str id "-button-remove-" range-id) (fn [] (remove-part id range-id)))
                        (util/on-click-once (str id "-button-up-" range-id) (fn [] (util/move-up (str id "-" range-id))))
                        (util/on-click-once (str id "-button-down-" range-id) (fn [] (util/move-down (str id "-" range-id)))))
                      (util/on-click (str id "-plus-above") (fn [] (add-to-vector new-level id spec-type true)))
                      (util/on-click (str id "-plus-under") (fn [] (add-to-vector new-level id spec-type false))))
     :validation-f #(let [f-map (get @map-edit-data id)]
                      (doseq [child-id (get-child-id-list id)] ((:validation-f (get f-map child-id)))))
     :get-value-f  #(let [f-map (get @map-edit-data id)]
                      (mapv (fn [child-id] ((:get-value-f (get f-map child-id)))) (get-child-id-list id)))
     }))

(defn get-language-map-html
  [id language part-html]
  [:div.notification {:id (str id "-" (name language))}
   [:div [:button.delete {:id (str id "-button-hide-" (name language))}]]
   [:p (language flags-map)]
   part-html])

(defn language-map-reducer
  [map [language part]]
  (assoc map language
             {:validation-f (:validation-f part)
              :get-value-f  (:get-value-f part)}))

(defmethod get-edit-map :language-map
  [level spec data]
  (let [new-level (+ 1 level)
        spec-form (s/form spec)
        spec-type (nth (nth spec-form 2) 2)
        all-languages (keys flags-map)
        language-map-data (spec data)
        parts (map #(vector % (get-edit-map new-level spec-type {spec-type (% language-map-data)})) all-languages)
        id (str "lg-map-" (swap! counter inc))
        function-map (reduce language-map-reducer {} parts)
        non-used-languages (remove (set (keys language-map-data)) all-languages)]
    (swap! map-edit-data #(assoc % id function-map))
    {:html         [:div
                    [:p.control
                     (for [language all-languages]
                       [:button.button {:id (str id "-button-toggle-" (name language))} (language flags-map)])
                     ]
                    [:div {:id id}
                     (map #(get-language-map-html id (first %) (:html (second %))) parts)]]
     :init-f       #(do
                      (doseq [language non-used-languages] (util/toggle-visibility (str id "-" (name language))))
                      (doseq [[_ part] parts] (if-let [part-function (:init-f part)] (part-function)))
                      (doseq [language all-languages]
                        (util/on-click (str id "-button-hide-" (name language)) (fn [] (util/hide (str id "-" (name language)))))
                        (util/on-click (str id "-button-toggle-" (name language)) (fn [] (util/toggle-visibility (str id "-" (name language)))))))
     :validation-f #(let [f-map (get @map-edit-data id)]
                      (doseq [language all-languages]
                        (if
                          (util/is-visible? (str id "-" (name language)))
                          ((get-in f-map [language :validation-f])))))
     :get-value-f  #(let [f-map (get @map-edit-data id)]
                      (into {} (map (fn [language] (if
                                                     (util/is-visible? (str id "-" (name language)))
                                                     [language ((get-in f-map [language :get-value-f]))])) all-languages)))
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
  [id default option-range]
  [:div.control {:id id}
   (for [it option-range]
     (let [checked (if (= it default) "true" nil)]
       [:label.radio {:id (str id "-" it)} [:input {:type "radio" :name id :checked checked :value it} it]]))])

(defn update-parts
  [option-range id get-radio-value]
  (let [radio-value (get-radio-value)]
    (doseq [it option-range]
      (if (= it radio-value)
        (util/show (str id "-" it))
        (util/hide (str id "-" it))))))

(defmethod get-edit-map :or
  [level spec data]
  (let [new-level (+ 1 level)
        id (str "or-" (swap! counter inc))
        [r-default or-maps] (reduce-kv (partial or-reducer data id new-level) [nil []] (into [] (rest spec)))
        default (if r-default r-default 0)
        radio-id (str id "-radio")
        get-radio-value #(int (util/get-radio-value radio-id))
        option-range (range (count or-maps))]
    {:html         [:div {:id id} (get-radio radio-id default option-range) (map-indexed #(get-or-html %1 default %2) or-maps)]
     :init-f       #(do
                      (doseq [element or-maps] (if-let [el-function (:init-f (second element))] (el-function)))
                      (doseq [it option-range] (util/on-change (str radio-id "-" it) (fn [] (update-parts option-range id get-radio-value)))))
     :validation-f #((:validation-f (nth or-maps (get-radio-value))))
     :get-value-f  #((:get-value-f (nth or-maps (get-radio-value))))
     }))