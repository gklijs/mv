(ns m-venue.image-selection
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defn show-hide-selection
  []
  (util/toggle-class :image-selection-button "is-inverted")
  (util/toggle-visibility :image-selection-box))

(defn select-image
  [clickEvent]
  (if-let [key (.-id (.-dataset (.-target clickEvent)))]
    (do
      (repo/set-item! "l-selected-image" key)
      (util/log (str "item from repo is " (repo/get-item key) " with key " key))
      (util/log (repo/get-item key))
      (if-let [i-reference (repo/get-item key)]
        (do
          (util/set-html nil (templates/responsive-image (second i-reference) "s" "selected-image"))
          (set! (.-background (.-style (dom/get-element :image-selection-button))) (str "url(" (:m-venue.spec/base-path (second i-reference)) "/36.jpg)"))
          ))
      )))

(defn init!
  "Initializes html and the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-selection)
  (if-let [i-info (repo/get-item "i-info")]
    (util/set-html :all-images-parent (templates/all-images (:m-venue.spec/latest-img (second i-info)))))
  (repo/set-renderer! #"i-info" #(util/set-html :all-images-parent (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (event/listen (dom/get-element :image-selection-box) :click select-image true))