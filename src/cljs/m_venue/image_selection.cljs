(ns m-venue.image-selection
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defn show-hide-selection
  []
  (util/toggle-class :image-selection-button "is-outlined")
  (util/toggle-visibility :image-selection-box))

(defn select-image
  [clickEvent]
  (if-let [key (.-id (.-dataset (.-target clickEvent)))]
    (do
      (repo/set-item! "l-selected-image" key)
      (repo/execute-with-map key #(do
                                    (util/set-html nil (templates/responsive-image (second %) "s" "selected-image"))
                                    (set! (.-background (.-style (dom/get-element :image-selection-button))) (str "url(" (:m-venue.spec/base-path (second %)) "/36.jpg)"))
                                    )))))

(defn init!
  "Initializes html and the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-selection)
  (repo/execute-with-map "i-info" #(util/set-html :all-images-parent (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (repo/set-renderer! #"i-info" #(util/set-html :all-images-parent (templates/all-images (:m-venue.spec/latest-img (second %)))))
  (event/listen (dom/get-element :image-selection-box) :click select-image true))