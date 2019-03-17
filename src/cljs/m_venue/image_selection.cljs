(ns m-venue.image-selection
  (:require [cljs.spec.alpha :as s]
            [m-venue.constants :refer [flags-map]]
            [m-venue.editor-templates :as editor-templates]
            [m-venue.repo :as repo]
            [m-venue.util :as util]
            [m-venue.templates :as templates]
            [m-venue.web-socket :refer [send-msg!]]))

(defonce selected-image (atom nil))
(defonce selected-image-key (atom nil))

(defn update-map
  [languages]
  (into {} (apply concat (for [ln languages]
                           [[[(keyword (str "title-" (name ln))) :m-venue.spec/title] (fn [m v] (assoc-in m [:m-venue.spec/img-meta-infos ln :m-venue.spec/title] v))]
                            [[(keyword (str "alt-" (name ln))) :m-venue.spec/alt] (fn [m v] (assoc-in m [:m-venue.spec/img-meta-infos ln :m-venue.spec/alt] v))]]))))

(defn add-if-not-empty
  [map [id spec] function]
  (if-let [input-element (util/ensure-element id)]
    (let [value (.-value input-element)]
      (if
        (s/valid? spec value)
        (function map value)
        map))
    map))

(defn save-image
  [image]
  (println (update-map (keys (:m-venue.spec/img-meta-infos image))))
  (if-let [new-map (reduce-kv add-if-not-empty @selected-image (update-map (keys (:m-venue.spec/img-meta-infos image))))]
    (do
      (repo/set-map! @selected-image-key :m-venue.spec/img-reference new-map)
      (reset! selected-image new-map))))

(defn add-or-remove-meta
  [image language]
  (let [new-map (if (get-in image [:m-venue.spec/img-meta-infos language])
                  (update image :m-venue.spec/img-meta-infos #(dissoc % language))
                  (update image :m-venue.spec/img-meta-infos #(assoc % language {:m-venue.spec/title "title" :m-venue.spec/alt "alt"})))]
    (repo/set-map! @selected-image-key :m-venue.spec/img-reference new-map)
    (reset! selected-image new-map)))

(add-watch selected-image nil
           (fn [_ _ _ ns]
             (util/set-html (editor-templates/image-meta-data (:m-venue.spec/img-meta-infos ns)))
             (util/on-click :image-save-button #(save-image ns))
             (doseq [lang (keys flags-map)]
               (util/on-click-once (keyword (str "switch-image-meta-" (name lang))) #(add-or-remove-meta ns lang)))
             ))

(defn show-hide-columns
  []
  (util/toggle-class :image-selection-button "is-outlined")
  (util/toggle-visibility :image-selection-columns))

(defn show-hide-edit
  []
  (util/toggle-visibility :image-edit))

(defn select-image
  [target]
  (if-let [key (.-id (.-dataset target))]
    (do
      (reset! selected-image-key key)
      (repo/execute-with-map key #(if-let [map (second %)]
                                    (do
                                      (util/set-html (templates/responsive-image map) :selected-image)
                                      (set! (.-src (util/ensure-element :small-selected-image))
                                            (str (:m-venue.spec/base-path map) "36.jpg"))
                                      (reset! selected-image map)))))))

(defn init!
  "Initializes html and the handlers"
  []
  (util/on-click :image-selection-button show-hide-columns)
  (repo/execute-with-map "i-summary" #(util/set-html (templates/all-images (second %))))
  (repo/set-renderer! #"i-summary" #(util/set-html (templates/all-images (second %))))
  (util/on-click-target :image-selection-columns #(select-image %))
  (util/on-click :selected-image show-hide-edit))