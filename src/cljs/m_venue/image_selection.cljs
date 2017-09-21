(ns m-venue.image-selection
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [cljs.spec.alpha :as s]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defonce selected-image (atom nil))
(defonce selected-image-key (atom nil))

(add-watch selected-image nil
           (fn [k r os ns]
             (set! (.-value (dom/get-element :title-nl)) "")
             (set! (.-value (dom/get-element :alt-nl)) "")
             (util/set-placeholder :title-nl (get-in ns [:m-venue.spec/title :m-venue.spec/nl-label]))
             (util/set-placeholder :alt-nl (get-in ns [:m-venue.spec/alt :m-venue.spec/nl-label]))))

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
      (reset! selected-image-key key)
      (repo/execute-with-map key #(if-let [[spec map] %]
                                    (do
                                      (util/set-html (templates/responsive-image map "m") :selected-image)
                                      (set! (.-background (.-style (dom/get-element :image-selection-button)))
                                            (str "url(" (:m-venue.spec/base-path map) "/36.jpg)"))
                                      (reset! selected-image map)
                                      ))))))
(def update-map
  {[:title-nl :m-venue.spec/nl-label] #(assoc-in %1 [:m-venue.spec/title :m-venue.spec/nl-label] %2)
   [:alt-nl  :m-venue.spec/nl-label] #(assoc-in %1 [:m-venue.spec/alt :m-venue.spec/nl-label] %2)})

(defn add-if-not-empty
  [map [id spec] function]
  (let [value (.-value (dom/get-element id))]
    (if
      (s/valid? spec value)
      (function map value)
      map)))

(defn save-image
  []
  (if-let [new-map (reduce-kv add-if-not-empty @selected-image update-map)]
    (do
      (repo/set-map! @selected-image-key :m-venue.spec/img-reference new-map)
      (reset! selected-image new-map)
      )))

(defn init!
  "Initializes html and the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-columns)
  (repo/execute-with-map "i-info" #(util/set-html (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (repo/set-renderer! #"i-info" #(util/set-html (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (event/listen (dom/get-element :image-selection-columns) :click select-image true)
  (event/listen (dom/get-element :selected-image) :click show-hide-edit)
  (event/listen (dom/get-element :image-save-button) :click save-image))