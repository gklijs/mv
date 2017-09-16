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

(defn init!
  "Initializes the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-selection)
  (repo/set-renderer! #"i-info" #(util/set-html :all-images-parent (templates/all-images (:m-venue.spec/latest-img (second %))))))