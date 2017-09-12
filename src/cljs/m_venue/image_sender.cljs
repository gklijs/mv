(ns m-venue.image-sender
  (:require [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [m-venue.web-socket :refer [send-msg!]]))

(defn toArray [js-col]
  (-> (clj->js [])
      (.-slice)
      (.call js-col)
      (js->clj)))

(defn upload-images
  []
  (let [file-selector (dom/get-element :upload-image-files)
        files (toArray (.-files file-selector))]
    (doseq [file files]
      (js/console.log file)
      (let [file-reader (js/FileReader.)]
        (set! (.-onloadend file-reader) (fn [] (send-msg! (.-result file-reader))))
        (.readAsArrayBuffer file-reader file)
        ))))

(defn init!
  "Initializes the handlers"
  []
  (event/listen (dom/get-element :upload-image-button) :click (fn [] (dom/click-element :upload-image-files)))
  (event/listen (dom/get-element :upload-image-files) :change upload-images))