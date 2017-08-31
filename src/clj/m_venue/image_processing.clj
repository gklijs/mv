(ns m-venue.image-processing
  (:require [clojure.test :refer :all]
            [image-resizer.crop :refer :all]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [image-resizer.resize :refer :all]
            [image-resizer.util :as util]))

(def original-image-path "resources/public/img/")
(def destination-image-path "resources/public/img/gen/")

(defn best-matching-class
  "gives the best matching css class given a ratio"
  [ratio]
  (cond
    (> ratio 1.85) "is-2by1"
    (> ratio 1.6) "is-16by9"
    (> ratio 1.4) "is-3by2"
    (> ratio 1.2)  "is-4by3"
    (> ratio 0.85)  "is-1by1"
    (> ratio 0.7) "is-3by4"
    (> ratio 0.6) "is-2by3"
    (> ratio 0.53) "is-9-by-16"
    :else "is-1by2"
    ))

(defn process
  "Renders the different kinds of variants needed for the site"
  [image-name]
  (let [buffered-image (util/buffered-image (clojure.java.io/file (str original-image-path image-name ".jpg")))
        [x-size y-size] (util/dimensions buffered-image)
        ratio (/ x-size y-size)
        css-class (best-matching-class ratio)
        square-intermediate (if (> ratio 1) (resize-to-height buffered-image 256) (resize-to-width buffered-image 256))
        [x-square y-square] (util/dimensions square-intermediate)
        square-buffered (if (> ratio 1)
                         (crop-from square-intermediate (/ (- x-square 256) 2) 0 256 256)
                         (crop-from square-intermediate 0 (/ (- y-square 256) 2) 256 256))
        big-square-image (format/as-file square-buffered (str destination-image-path image-name "-b-square.jpg") :verbatim)
        small-square-image (format/as-file
                             (resize-to-width square-buffered 64)
                             (str destination-image-path image-name "-s-square.jpg")
                             :verbatim)
        small-image (format/as-file
                      (resize-to-width buffered-image 278)
                      (str destination-image-path image-name "-small.jpg")
                      :verbatim)
        medium-image (format/as-file
                       (resize-to-width buffered-image 449)
                       (str destination-image-path image-name "-medium.jpg")
                       :verbatim)
        large-image (format/as-file
                      (resize-to-width buffered-image 962)
                      (str destination-image-path image-name "-large.jpg")
                      :verbatim)]
    [css-class big-square-image small-square-image small-image medium-image large-image]
    ))




