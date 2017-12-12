(ns m-venue.image-test
  (:require [clojure.test :refer :all]
            [image-resizer.util :as util]
            [m-venue.image-processing :as processing]
            [m-venue.spec])
  (:import (java.nio.file Files)))

(deftest image-test
  (testing "dimensions-function"
    (let [[width height] (util/dimensions (util/buffered-image (clojure.java.io/file "test/resources/img/cat_in_a_box.jpg")))]
      (is (= 1536 width) "width should be image width")
      (is (= 2048 height) "height should be image height")))
  (testing "processing"
    (let [byte-array (Files/readAllBytes (.toPath (clojure.java.io/file "test/resources/img/cat_in_a_box.jpg")))
          result (processing/process byte-array)]
      (is (= "geti-info:[:m-venue.spec/img-info [[\"resources/public/img/\" 11] []]]" (first result)))
      (is (= "geti-11:[:m-venue.spec/img-reference [[1536 2048 \"is-3by4\" \"/img/11/\"] [nil nil]]]" (second result)))))
  (testing "processing-small-image"
    (let [byte-array (Files/readAllBytes (.toPath (clojure.java.io/file "test/resources/img/celebration.png")))
          result (processing/process byte-array)]
      (is (= "geti-info:[:m-venue.spec/img-info [[\"resources/public/img/\" 12] []]]" (first result)))
      (is (= "geti-12:[:m-venue.spec/img-reference [[225 225 \"is-1by1\" \"/img/12/\"] [nil nil]]]" (second result))))))