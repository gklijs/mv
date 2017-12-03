(ns m-venue.image-processing
  (:import java.io.ByteArrayInputStream)
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [image-resizer.crop :refer :all]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [image-resizer.resize :refer :all]
            [image-resizer.util :as util]
            [m-venue.constants :refer [image-sizes]]
            [m-venue.repo :as repo]))

(defn best-matching-class
  "gives the best matching css class given a ratio"
  [ratio]
  (cond
    (> ratio 1.85) "is-2by1"
    (> ratio 1.6) "is-16by9"
    (> ratio 1.4) "is-3by2"
    (> ratio 1.2) "is-4by3"
    (> ratio 0.85) "is-1by1"
    (> ratio 0.7) "is-3by4"
    (> ratio 0.6) "is-2by3"
    (> ratio 0.53) "is-9-by-16"
    :else "is-1by2"
    ))

(defn process
  "Renders the different kinds of variants needed for the site"
  [byte-array]
  (let [inputstream (ByteArrayInputStream. byte-array)
        buffered-image (util/buffered-image inputstream)
        [x-size y-size] (util/dimensions buffered-image)
        ratio (/ x-size y-size)
        css-class (best-matching-class ratio)
        img-info (second (repo/get-map "i-info"))
        destination-image-path (:m-venue.spec/img-path img-info)
        new-img-latest (inc (:m-venue.spec/latest-img img-info))
        path (str destination-image-path new-img-latest "/")
        set-new-img-info (repo/set-map! "i-info" :m-venue.spec/img-info (assoc img-info :m-venue.spec/latest-img new-img-latest))
        create-parents (io/make-parents (io/file (str path "o.jpg")))
        new-img-key (str "i-" new-img-latest)
        set-img (repo/set-map! new-img-key :m-venue.spec/img-reference
                               {:m-venue.spec/x-size        x-size
                               :m-venue.spec/y-size        y-size
                               :m-venue.spec/img-css-class css-class
                               :m-venue.spec/base-path     (str "/img/" new-img-latest "/")})
        original-image (format/as-file
                         buffered-image
                         (str path "o.jpg")
                         :verbatim)
        square-intermediate (if (> ratio 1) (resize-to-height buffered-image 256) (resize-to-width buffered-image 256))
        [x-square y-square] (util/dimensions square-intermediate)
        square-buffered (if (> ratio 1)
                          (crop-from square-intermediate (/ (- x-square 256) 2) 0 256 256)
                          (crop-from square-intermediate 0 (/ (- y-square 256) 2) 256 256))
        big-square-image (format/as-file square-buffered (str path "256.jpg") :verbatim)
        small-square-image (format/as-file
                             (resize-to-width square-buffered 64)
                             (str path "64.jpg")
                             :verbatim)
        button-image (format/as-file
                             (resize-to-width square-buffered 36)
                             (str path "36.jpg")
                             :verbatim)]
    (doseq [[name value] image-sizes]
      (if (> x-size value)
        (format/as-file (resize-to-width buffered-image value)
                        (str path name ".jpg")
                        :verbatim)))
    [(str "seti-info:" (repo/get-string "i-info")) (str "set" new-img-key ":" (repo/get-string new-img-key))]))

