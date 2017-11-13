(ns m-venue.main-content-edit
  (:require [m-venue.editor :as editor]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]])
  (:import (goog.events EventTarget EventType)))

(defn view-edit-switch
  []
  (util/toggle-class :edit-main-button "is-outlined")
  (util/toggle-visibility :main-content-edit))

(defn init!
  "Initializes html and the handlers"
  []
  (util/on-click :edit-main-button view-edit-switch)
  (util/on-click-once :edit-html-button-1 #(editor/init! 1)))