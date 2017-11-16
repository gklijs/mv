(ns m-venue.map-edit-primitives
  (:require [cljs.spec.alpha :as s]
            [m-venue.util :as util]
            [m-venue.spec :as spec]
            [m-venue.editor :as editor]
            [m-venue.editor-templates :as et]))

(defn get-if-valid
  [spec value]
  (if
    (s/valid? spec value)
    value
    nil))

(defmulti get-primitive
          "Handles serialize on or-ed key part"
          (fn [id type initial-value]
            (cond
              (= type spec/label) :label
              (= type spec/html) :html
              :else :label
              )))

(defmethod get-primitive :label
  [id type initial-value]
  (let [main-id (str "edit-label-" id)]
    {:html         [:div.control [:input.input {:type "text" :id main-id :value initial-value}]]
     :validation-f #(do (util/log (str "type: " type ", id: " main-id))
                        (if
                          (s/valid? type (.-value (util/ensure-element main-id)))
                          (util/remove-class! main-id "is-warning")
                          (util/add-class! main-id "is-warning")))
     :get-value-f  #(get-if-valid type (.-value (util/ensure-element main-id)))}
    ))

(defmethod get-primitive :html
  [id type initial-value]
  (let [value-id (str "edit-me-" id)]
    {:html         (et/html-edit id initial-value)
     :init-f       #(util/on-click-once (str "edit-html-button-" id) (fn [] (editor/init! id)))
     :validation-f #(do (util/log (str "type: " type ", id: " value-id))
                        (if
                          (s/valid? type (.-value (util/ensure-element value-id)))
                          (util/remove-class! value-id "is-warning")
                          (util/add-class! value-id "is-warning")))
     :get-value-f  #(get-if-valid type (.-value (util/ensure-element value-id)))}
    ))
