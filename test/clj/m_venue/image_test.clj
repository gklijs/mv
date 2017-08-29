(ns m-venue.image-test
  (:require [clojure.test :refer :all]
            [image-resizer.crop :refer :all]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [image-resizer.resize :refer :all]
            [image-resizer.rotate :refer :all]
            [image-resizer.pad :refer :all]))

(deftest image-test
  (testing "get-spec"
    (let [result     (format/as-file
                       (force-resize (clojure.java.io/file "resources/public/img/cat_in_a_box.jpg") 256 256)
                       "resources/public/img/gen/cat_in_a_box.jpg")]
      (println (str "Image is: " result)))
    (is (= 404 404)))
  (testing "get-spec"
    (let [result     (format/as-file
                       (resize-to-width (clojure.java.io/file "resources/public/img/cat_in_a_box.jpg") 250)
                       "resources/public/img/gen/cat_in_a_box.jpg")]
      (println (str "Image is: " result)))
    (is (= 404 404))))




