(ns m-venue.image-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [image-resizer.util :as util]
            [m-venue.image-processing :as processing]
            [m-venue.spec]
            [m-venue.test-data]
            [clojure.string :as string])
  (:import (java.nio.file Files)))

(deftest image-test
  (testing "dimensions-function"
    (let [[width height] (util/dimensions (util/buffered-image (clojure.java.io/file "test/resources/img/cat_in_a_box.jpg")))]
      (is (= 1536 width) "width should be image width")
      (is (= 2048 height) "height should be image height")))
  (testing "processing"
    (let [byte-array (Files/readAllBytes (.toPath (clojure.java.io/file "test/resources/img/cat_in_a_box.jpg")))
          result (processing/process byte-array)]
      (is (= 1536 (nth result 0)))
      (is (= 2048 (nth result 1)))
      (is (= "is-3by4" (nth result 2)))
      (is (string/ends-with? (nth result 3) "/resources/public/img/11/o.jpg"))
      (is (string/ends-with? (nth result 4) "/resources/public/img/11/256.jpg"))
      (is (string/ends-with? (nth result 5) "/resources/public/img/11/64.jpg"))
      (is (string/ends-with? (nth result 6) "/resources/public/img/11/s.jpg"))
      (is (string/ends-with? (nth result 7) "/resources/public/img/11/m.jpg"))
      (is (string/ends-with? (nth result 8) "/resources/public/img/11/l.jpg"))))
  (testing "processing-small-image"
    (let [byte-array (Files/readAllBytes (.toPath (clojure.java.io/file "test/resources/img/celebration.png")))
          result (processing/process byte-array)]
      (is (= 225 (nth result 0)))
      (is (= 225 (nth result 1)))
      (is (= "is-1by1" (nth result 2)))
      (is (string/ends-with? (nth result 3) "/resources/public/img/12/o.jpg"))
      (is (string/ends-with? (nth result 4) "/resources/public/img/12/256.jpg"))
      (is (string/ends-with? (nth result 5) "/resources/public/img/12/64.jpg"))
      (is (= (nth result 6) nil))
      (is (= (nth result 7) nil))
      (is (= (nth result 8) nil)))))