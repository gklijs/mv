(ns m-venue.image-sender
  (:require [m-venue.util :as util]
            [m-venue.web-socket :refer [send-msg!]]))

(defn toArray [js-col]
  (-> (clj->js [])
      (.-slice)
      (.call js-col)
      (js->clj)))

(defn upload-images
  []
  (let [file-selector (util/ensure-element :upload-image-files)
        files (toArray (.-files file-selector))]
    (doseq [file files]
      (let [file-reader (js/FileReader.)]
        (set! (.-onloadend file-reader) #(send-msg! (.-result file-reader)))
        (.readAsArrayBuffer file-reader file)
        ))))

(defn init!
  "Initializes the handlers"
  []
  (util/on-click :upload-image-button #(.click (util/ensure-element :upload-image-files)))
  (util/on-change :upload-image-files upload-images))