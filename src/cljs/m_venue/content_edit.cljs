(ns m-venue.content-edit
  (:require [m-venue.editor :as editor]
            [m-venue.map-edit :refer [get-edit-map reset-map-edit-data]]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.spec :as spec]
            [m-venue.templates :as templates]))

(defonce remove-keys (atom nil))

(defn view-edit-switch
  []
  (util/toggle-class :edit-main-button "is-outlined")
  (util/toggle-visibility :main-content-edit))

(defn stop-edit
  []
  (util/set-html nil :edit-box)
  (util/enable :start-main-edit-button)
  (util/enable :start-menu-edit-button)
  (util/disable :stop-edit-button)
  (util/disable :verify-edit-button)
  (util/disable :play-edit-button)
  (util/disable :save-edit-button)
  (doseq [key @remove-keys] (util/unlisten-by-key key))
  (reset-map-edit-data))

(defn set-content
  [id spec value]
  (cond
    (= spec ::spec/gen-doc) (util/set-html (templates/gd-content id value))
    :else (util/log (str "could not set data " value "with id " id "because unknown spec: " spec))
    ))

(defn play
  [id spec get-value-f]
  (if-let [value (get-value-f)]
    (set-content id spec value)))

(defn save
  [id spec get-value-f]
  (if-let [value (get-value-f)]
    (do
      (set-content id spec value)
      (repo/set-map! id spec value)
      (stop-edit))))

(defn start-edit
  [id main-data]
  (let [edit-map (get-edit-map (first main-data) (second main-data))]
    (util/disable :start-main-edit-button)
    (util/disable :start-menu-edit-button)
    (let [key1 (util/on-click :verify-edit-button (:validation-f edit-map))
          key2 (util/on-click :play-edit-button #(play id (first main-data) (:get-value-f edit-map)))
          key3 (util/on-click :save-edit-button #(save id (first main-data) (:get-value-f edit-map)))]
      (reset! remove-keys [key1 key2 key3])
      )
    (util/on-click-once :stop-edit-button stop-edit)
    (util/enable :stop-edit-button)
    (util/enable :verify-edit-button)
    (util/enable :play-edit-button)
    (util/enable :save-edit-button)
    (util/set-html (:html edit-map) :edit-box)
    ((:init-f edit-map))))

(defn init!
  "Initializes html and the handlers"
  []
  (util/on-click :edit-main-button view-edit-switch)
  (let [id (util/get-data "main-content" "document")]
    (util/on-click :start-main-edit-button #(repo/execute-with-map id (partial start-edit id))))
  (let [id (str "n-main-" (util/get-language))]
    (util/on-click :start-menu-edit-button #(repo/execute-with-map id (partial start-edit id)))))