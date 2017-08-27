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
                       (resize (clojure.java.io/file "resources/public/img/cat_in_a_box.jpg") 100 100)
                       "/tmp/cat_in_a_box.jpg")]
      (println (str "Image is: " result) result))
    (is (= 404 404))))




