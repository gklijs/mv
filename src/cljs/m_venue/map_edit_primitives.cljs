(ns m-venue.map-edit-primitives
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as string]
            [m-venue.util :as util]
            [m-venue.spec :as spec]
            [m-venue.editor-templates :as et]
            [m-venue.image-selection :refer [selected-image-key]]
            [m-venue.repo :as repo]))

(defn get-if-valid
  [spec get-function]
  (if-let [value (get-function)]
    (if (s/valid? spec value)
      value
      (util/log (str s/explain spec value)))))

(defn validate
  [spec get-function id]
  (if
    (s/valid? spec (get-function))
    (util/remove-class! id "is-warning")
    (util/add-class! id "is-warning")))

(defmulti get-primitive
          "Handles serialize on or-ed key part"
          (fn [_ spec _]
            (let [spec-form (s/form spec)]
              (cond
                (= ::spec/label spec) :label
                (= ::spec/html spec) :html
                (set? spec-form) :set
                (= ::spec/img spec) :img
                :else :label))))

(defmethod get-primitive :label
  [id spec initial-value]
  (let [value-id (str "edit-label-" id)
        get-function (fn [] (.-value (util/ensure-element value-id)))]
    {:html         [:div.field.is-grouped
                    [:input.input {:type "text" :id value-id :value initial-value}]
                    [:label.label {:style "white-space: nowrap;"} spec]]
     :validation-f #(validate spec get-function value-id)
     :get-value-f  #(get-if-valid spec get-function)}))

(defmethod get-primitive :html
  [id spec initial-value]
  (let [value-id (str "ckeditor-" id)
        get-function (fn [] (.-innerHTML (util/ensure-element value-id)))]
    {:html         [:div {:id value-id} initial-value]
     :init-f       #(.create js/BalloonEditor
                             (.querySelector js/document (str "#" value-id))
                             #js {"toolbar" #js ["bold" "italic" "link" "numberedlist" "bulletedlist" "|" "undo" "redo"]})
     :validation-f #(validate spec get-function value-id)
     :get-value-f  #(get-if-valid spec get-function)}))

(defmethod get-primitive :set
  [id spec initial-value]
  (let [value-id (str "edit-style-" id)
        warning-id (str "edit-style-span" id)
        set-for-spec (sort (s/form spec))
        get-function (fn [] (keyword (.-value (util/ensure-element value-id))))]
    {:html         [:div.control [:span.select {:id warning-id} [:select {:id value-id}
                                                                 (for [item set-for-spec] [:option (if (= item initial-value) {:selected "selected"}) item])]]]
     :validation-f #(validate spec get-function warning-id)
     :get-value-f  #(get-if-valid spec get-function)}))

(defmethod get-primitive :img
  [id spec initial-value]
  (let [value-id (str "edit-img-" id)
        get-function (fn [] (int (.-value (util/ensure-element value-id))))
        img-id (str value-id "-image")
        set-img-f #(set! (.-src (util/ensure-element img-id)) %)
        set-value-f #(set! (.-value (util/ensure-element value-id)) %)]
    {:html         [:div.field.is-grouped.is-grouped-multiline
                    [:p.control [:img {:id img-id}]]
                    (et/button (str value-id "-remove") :3 "close-circle")
                    (et/button (str value-id "-set") :3 "image")
                    [:div.control [:input.input {:type "text" :id value-id :value initial-value :maxlength 6 :size 6 :style "width:auto"}]]]
     :init-f       #(do
                      (if initial-value (repo/execute-with-map (str "i-" initial-value)
                                                               (fn [[_ map]] (set-img-f (str (:m-venue.spec/base-path map) "36.jpg")))))
                      (util/on-change value-id (fn [] (set-img-f
                                                        (if-let [value (repo/get-map :i (get-function))]
                                                          (str (:m-venue.spec/base-path (second value)) "36.jpg") ""))))
                      (util/on-click (str value-id "-remove") (fn [] (set-value-f "")))
                      (util/on-click (str value-id "-set") (fn [] (set-value-f (second (string/split @selected-image-key #"-" 2))))))
     :validation-f #(validate spec get-function value-id)
     :get-value-f  #(get-if-valid spec get-function)}))