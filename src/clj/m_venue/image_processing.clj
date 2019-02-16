(ns m-venue.image-processing
  (:import java.io.ByteArrayInputStream
           org.apache.commons.io.IOUtils
           (java.util Base64)
           (java.time Instant))
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [image-resizer.crop :refer :all]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [image-resizer.resize :refer :all]
            [image-resizer.util :as util]
            [m-venue.constants :refer [image-sizes relative-height-map]]
            [m-venue.repo :as repo]))

(defn best-matching-class
  [ratio]
  (condp < ratio
    2.5 "is-3by1"
    1.88 "is-2by1"
    1.72 "is-16by9"
    1.58 "is-5by3"
    1.42 "is-3by2"
    1.29 "is-4by3"
    1.13 "is-5by4"
    0.9 "is-1by1"
    0.78 "is-4by5"
    0.71 "is-3by4"
    0.63 "is-2by3"
    0.58 "is-3by5"
    0.53 "is-9by16"
    0.42 "is-1by2"
    "is-1by3"))

(defn get-corrected-image [x-size y-size buffered-image css-class]
  (let [rel-height (get relative-height-map css-class)
        x-according-y (/ y-size rel-height)
        y-according-x (* x-size rel-height)
        dif-x (- x-size x-according-y)
        dif-y (- y-size y-according-x)]
    (cond
      (> dif-x 2) [(crop-from buffered-image (/ dif-x 2) 0 x-according-y y-size) x-according-y]
      (> dif-y 2) [(crop-from buffered-image 0 (/ dif-y 2) x-size y-according-x) x-size]
      :else [buffered-image x-size])))

(defn to-base-64-encoding
  [buffered-image]
  (let [^ByteArrayInputStream stream (format/as-stream buffered-image "jpg")
        bytes (IOUtils/toByteArray stream)
        encoder (Base64/getEncoder)]
    (.encodeToString encoder bytes)))

(defn add-base-64
  [result [first second]]
  (println "first: " first)
  (println "second: " second)
  (let [base-10-number (Long/valueOf (str first second) 8)]
    (condp > base-10-number
      10 (str result base-10-number)
      36 (str result (char (+ base-10-number 55)))
      62 (str result (char (+ base-10-number 61)))
      63 (str result "-")
      (str result "_"))))

(defn int->path
  [number]
  (let [base-8 (Long/toString number 8)
        base-8-with-zero (if (= 0 (mod (count base-8) 2)) base-8 (str "0" base-8))]
    (reduce add-base-64 "" (partition 2 base-8-with-zero))))

(defn summary-update
  [summary]
  (let [update-counter (update summary :m-venue.spec/latest-img inc)
        new-img-latest (:m-venue.spec/latest-img update-counter)
        new-image-summary {:m-venue.spec/img new-img-latest
                           :m-venue.spec/img-uploaded-timestamp (.toEpochMilli (Instant/now))
                           :m-venue.spec/base-path (str "/img/" (int->path new-img-latest) "/")}]
    (update summary :m-venue.spec/all-images assoc (keyword (str new-img-latest)) new-image-summary)))

(defn process
  "Renders the different kinds of variants needed for the site"
  [byte-array]
  (let [inputstream (ByteArrayInputStream. byte-array)
        buffered-image (util/buffered-image inputstream)
        [x-size y-size] (util/dimensions buffered-image)
        ratio (/ x-size y-size)
        css-class (best-matching-class ratio)
        img-summary (repo/update-map! :i :summary summary-update)
        destination-image-path (:m-venue.spec/img-path img-summary)
        new-img-latest (:m-venue.spec/latest-img img-summary)
        path (str destination-image-path (int->path new-img-latest) "/")
        _ (io/make-parents (io/file (str path "o.jpg")))
        _ (format/as-file buffered-image (str path "o.jpg") :verbatim)
        square-intermediate (if (> ratio 1) (resize-to-height buffered-image 256) (resize-to-width buffered-image 256))
        [x-square y-square] (util/dimensions square-intermediate)
        square-buffered (if (> ratio 1)
                          (crop-from square-intermediate (/ (- x-square 256) 2) 0 256 256)
                          (crop-from square-intermediate 0 (/ (- y-square 256) 2) 256 256))
        _ (format/as-file square-buffered (str path "256.jpg") :verbatim)
        _ (format/as-file (resize-to-width square-buffered 64)
                          (str path "64.jpg")
                          :verbatim)
        small-square (resize-to-width square-buffered 36)
        _ (format/as-file small-square (str path "36.jpg") :verbatim)
        _ (repo/set-map! :i new-img-latest :m-venue.spec/img-reference
                         {:m-venue.spec/x-size         x-size
                          :m-venue.spec/y-size         y-size
                          :m-venue.spec/img-css-class  css-class
                          :m-venue.spec/base-path      (str "/img/" (int->path new-img-latest) "/")
                          :m-venue.spec/base-64        (to-base-64-encoding small-square)
                          :m-venue.spec/base-64-square (to-base-64-encoding (resize-to-width buffered-image 36))})
        [corrected-image corr-x] (get-corrected-image x-size y-size buffered-image css-class)]
    (if
      (> corr-x (:s image-sizes))
      (doseq [[key value] image-sizes]
        (if (> corr-x value)
          (format/as-file (resize-to-width corrected-image value)
                          (str path (name key) ".jpg")
                          :verbatim)))
      (format/as-file corrected-image (str path "s.jpg") :verbatim))
    [(str "seti-summary:" (repo/get-string :i :summary)) (str "set" "i-" new-img-latest ":" (repo/get-string :i new-img-latest))]))

