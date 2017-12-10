(ns m-venue.basic
  (:require [m-venue.util :as util]))

(defn toggle
  []
  (util/toggle-class :burger-menu "is-active")
  (util/toggle-class :main-menu "is-active"))

(defn toggle-side
  []
  (util/toggle-class :burger-side-content "is-active")
  (util/toggle-class :side-content "is-hidden-mobile"))

(defn init!
  "Initializes basic ui listeners"
  []
  (util/on-click :burger-menu toggle)
  (util/on-click :burger-side-content toggle-side))