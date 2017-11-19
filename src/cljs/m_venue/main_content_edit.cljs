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

(defn play
  [id get-value-f]
  (if-let [value (get-value-f)]
      (util/set-html (templates/gd-content id value))))

(defn save
  [id spec get-value-f]
  (if-let [value (get-value-f)]
    (do
      (util/set-html (templates/gd-content id value))
      (repo/set-map! id spec value)
      (stop-edit))))

(defn start-edit
  [id main-data]
  (let [edit-map (get-edit-map (first main-data) (second main-data))]
    (util/disable :start-edit-button)
    (util/enable :stop-edit-button)
    (util/on-click-once :stop-edit-button stop-edit)
    (util/enable :verify-edit-button)
    (util/on-click :verify-edit-button (:validation-f edit-map))
    (util/enable :play-edit-button)
    (util/on-click :play-edit-button #(play id (:get-value-f edit-map)))
    (util/enable :save-edit-button)
    (util/on-click :save-edit-button #(save id (first main-data) (:get-value-f edit-map)))
    (util/set-html (:html edit-map) :edit-box)
    ((:init-f edit-map))))

(defn init!
  "Initializes html and the handlers"
  []
  (util/on-click :edit-main-button view-edit-switch)
  (let [id (util/get-data "main-content" "document")]
    (util/on-click :start-edit-button #(repo/execute-with-map id (partial start-edit id)))))