(ns m-venue.image-selection
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defn show-hide-columns
  []
  (util/toggle-class :image-selection-button "is-outlined")
  (util/toggle-visibility :image-selection-columns))

(defn show-hide-edit
  []
  (util/toggle-visibility :image-edit))

(defn select-image
  [clickEvent]
  (if-let [key (.-id (.-dataset (.-target clickEvent)))]
    (do
      (set! (.-value (dom/get-element :title-nl)) "")
      (set! (.-value (dom/get-element :alt-nl)) "")
      (repo/set-item! "l-selected-image" key)
      (repo/execute-with-map key #(if-let [[spec map] %]
                                    (do
                                      (util/set-html (templates/responsive-image map "s") :selected-image)
                                      (set! (.-background (.-style (dom/get-element :image-selection-button)))
                                            (str "url(" (:m-venue.spec/base-path map) "/36.jpg)"))
                                      (util/set-placeholder :title-nl (get-in map [:m-venue.spec/title :m-venue.spec/nl-label]))
                                      (util/set-placeholder :alt-nl (get-in map [:m-venue.spec/alt :m-venue.spec/nl-label]))
                                      ))))))
(def update-map
  {:title-nl #(assoc-in %1 [:m-venue.spec/title :m-venue.spec/nl-label] %2)
   :alt-nl   #(assoc-in %1 [:m-venue.spec/alt :m-venue.spec/nl-label] %2)})

(defn add-if-not-empty
  [map id function]
  (let [value (.-value (dom/get-element id))]
    (if
      (and (not (nil? value)) (> (count value) 0))
      (function map value)
      map)))

(defn save-image
  []
  (let [selected-image (repo/get-item "l-selected-image")
        new-map (reduce-kv add-if-not-empty (second (repo/get-map selected-image)) update-map)]
    (repo/set-map selected-image :m-venue.spec/img-reference new-map)
    (set! (.-value (dom/get-element :title-nl)) "")
    (set! (.-value (dom/get-element :alt-nl)) "")
    (util/set-placeholder :title-nl (get-in new-map [:m-venue.spec/title :m-venue.spec/nl-label]))
    (util/set-placeholder :alt-nl (get-in new-map [:m-venue.spec/alt :m-venue.spec/nl-label]))
    ))

(defn init!
  "Initializes html and the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-columns)
  (repo/execute-with-map "i-info" #(util/set-html (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (repo/set-renderer! #"i-info" #(util/set-html (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (event/listen (dom/get-element :image-selection-columns) :click select-image true)
  (event/listen (dom/get-element :selected-image) :click show-hide-edit)
  (event/listen (dom/get-element :image-save-button) :click save-image))