(ns m-venue.main-content-edit
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defn view-edit-switch
  []
  (util/toggle-class :edit-main-button "is-outlined")
  (util/toggle-visibility :main-content-edit)
  (util/toggle-visibility :main-content))

(defn init!
  "Initializes html and the handlers"
  []
  (event/listen (dom/get-element :edit-main-button) :click view-edit-switch))