(ns m-venue.main-content-edit
  (:require [m-venue.editor :as editor]
            [m-venue.map-edit :refer [get-edit-map reset-map-edit-data]]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]])
  (:import (goog.events EventTarget EventType)))

(defn view-edit-switch
  []
  (util/toggle-class :edit-main-button "is-outlined")
  (util/toggle-visibility :main-content-edit))

(defn stop-edit
  []
  (util/set-html nil :edit-box)
  (util/enable :start-edit-button)
  (util/disable :stop-edit-button)
  (util/disable :verify-edit-button)
  (util/disable :play-edit-button)
  (util/disable :save-edit-button)
  (reset-map-edit-data))

(defn start-edit
  [main-data]
  (let [edit-map (get-edit-map (first main-data) (second main-data))]
    (util/disable :start-edit-button)
    (util/enable :stop-edit-button)
    (util/on-click-once :stop-edit-button stop-edit)
    (util/enable :verify-edit-button)
    (util/on-click :verify-edit-button (:validation-f edit-map))
    (util/enable :play-edit-button)
    (util/enable :save-edit-button)
    (util/set-html (:html edit-map) :edit-box)
    ((:init-f edit-map))))

(defn init!
  "Initializes html and the handlers"
  []
  (util/on-click :edit-main-button view-edit-switch)
  (util/on-click :start-edit-button #(repo/execute-with-map "p-home" start-edit)))