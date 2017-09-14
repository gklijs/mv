(ns m-venue.util
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]))

(defn log
  [x]
  (js/console.log x))

(defn toggle-class [id toggled-class]
  (let [element (dom/ensure-element id)
        el-classList (.-classList (dom/ensure-element element))]
    (if (.contains el-classList toggled-class)
      (.remove el-classList toggled-class)
      (.add el-classList toggled-class))))

(defn toggle-visibility [id]
  (let [element (dom/ensure-element id)
        style-display (.-display (.-style element))]
    (if (= "none" style-display)
      (set! (.-display (.-style element)) "")
      (set! (.-display (.-style element)) "none"))))