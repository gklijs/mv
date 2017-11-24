(ns m-venue.map-edit-primitives
  (:require [cljs.spec.alpha :as s]
            [m-venue.util :as util]
            [m-venue.spec :as spec]
            [m-venue.editor :as editor]
            [m-venue.editor-templates :as et]))

(defn get-if-valid
  [spec get-function]
  (if-let
    [value (get-function)]
    (if
      (s/valid? spec value)
      value)))

(defn validate
  [spec get-function id]
  (if
    (s/valid? spec (get-function))
    (util/remove-class! id "is-warning")
    (util/add-class! id "is-warning")))

(defmulti get-primitive
          "Handles serialize on or-ed key part"
          (fn [id spec initial-value]
            (let [spec-form (s/form spec)]
              (cond
                (= spec-form (s/form spec/label)) :label
                (= spec-form (s/form spec/html)) :html
                (= ::spec/style spec) :style
                (= ::spec/img spec) :img
                :else :label
                ))))

(defmethod get-primitive :label
  [id spec initial-value]
  (let [value-id (str "edit-label-" id)
        get-function (fn [] (.-value (util/ensure-element value-id)))]
    {:html         [:div.control [:input.input {:type "text" :id value-id :value initial-value}]]
     :validation-f #(validate spec get-function value-id)
     :get-value-f  #(get-if-valid spec get-function)}
    ))

(defmethod get-primitive :html
  [id spec initial-value]
  (let [get-function (fn [] (.-innerHTML (util/ensure-element (str "edit-me-" id))))]
    {:html         (et/html-edit id initial-value)
     :init-f       #(util/on-click-once (str "edit-html-button-" id) (fn [] (editor/init! id)))
     :validation-f #(validate spec get-function (str "html-edit-" id))
     :get-value-f  #(get-if-valid spec get-function)}
    ))

(defmethod get-primitive :style
  [id spec initial-value]
  (let [value-id (str "edit-style-" id)
        warning-id (str "edit-style-span" id)
        get-function (fn []  (keyword (.-value (util/ensure-element value-id))))]
    {:html         [:div.control [:span.select {:id warning-id}[:select {:id value-id}
                                                [:option (if (= :0 initial-value) {:selected "selected"}) 0]
                                                [:option (if (= :1 initial-value) {:selected "selected"}) 1]
                                                [:option (if (= :2 initial-value) {:selected "selected"}) 2]
                                                [:option (if (= :3 initial-value) {:selected "selected"}) 3]
                                                [:option (if (= :4 initial-value) {:selected "selected"}) 4]
                                                [:option (if (= :5 initial-value) {:selected "selected"}) 5]]]]
     :validation-f #(validate spec get-function warning-id)
     :get-value-f  #(get-if-valid spec get-function)}))

(defmethod get-primitive :img
  [id spec initial-value]
  (let [value-id (str "edit-img-" id)
        get-function (fn [] (.-value (util/ensure-element value-id)))]
    {:html         [:div.control [:input.input {:type "text" :id value-id :value initial-value}]]
     :validation-f #(validate spec get-function value-id)
     :get-value-f  #(get-if-valid spec get-function)}
    ))
