(ns m-venue.img-doc
  (:require [m-venue.util :as util]
            [m-venue.templates :as templates]))

(defn enlarge
  [target]
  (let [gf-data (-> target
                    .-parentElement
                    .-parentElement
                    .-dataset)]
    (if-let [src (.-src gf-data)]
      (let [title (.-title gf-data)
            alt (.-alt gf-data)
            x (.-x gf-data)
            y (.-y gf-data)
            [vh vw] (util/get-viewport-size)
            scale (if
                    (< x vh)
                    100
                    (* 100 (/ x y) (/ vh vw)))]
        (util/set-html (templates/image-modal src title alt scale) :main-content false)
        (util/on-click-once :image-modal #(util/remove-node :image-modal))))))

(defn init!
  "Initializes basic ui listeners"
  []
  (util/on-click-all-by-class "enlargeable-image" #(enlarge %)))