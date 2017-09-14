(ns m-venue.image-selection
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.util :as util]
            [m-venue.web-socket :refer [send-msg!]]))

(defn show-hide-selection
  []
  (util/toggle-class :image-selection-button "is-inverted")
  (util/toggle-visibility :image-selection-box))

(defn init!
  "Initializes the handlers"
  []
  (event/listen (dom/get-element :image-selection-button) :click show-hide-selection))