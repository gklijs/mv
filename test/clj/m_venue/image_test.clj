(ns m-venue.image-test
  (:require [clojure.test :refer :all]
            [m-venue.image-processing :as processing]
            [image-resizer.util :as util]))

(deftest image-test
  (testing "dimensions-function"
    (let [[width height] (util/dimensions (util/buffered-image (clojure.java.io/file "resources/public/img/cat_in_a_box.jpg")))]
      (is (= 1536 width) "width should be image width")
      (is (= 2048 height) "height should be image height")))
  (testing "processing"
    (let [result (processing/process "cat_in_a_box")]
      (println (str "Result is: " result)))
    (is (= 404 404))))




