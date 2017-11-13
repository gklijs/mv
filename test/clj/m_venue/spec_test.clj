(ns m-venue.spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.spec :refer :all]
            [spec-serialize.core :as tf]))

(def correct-label {:m-venue.spec/nl-label "Katten"})
(def correct-tile {:m-venue.spec/title {:m-venue.spec/nl-label "Blokje over katten"}
                   :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                   :m-venue.spec/style :1
                   :m-venue.spec/href  "www.nu.nl"
                   :m-venue.spec/img   1})
(def correct-gen-doc {:m-venue.spec/tile {:m-venue.spec/title     {:m-venue.spec/nl-label "Alles over katten"}
                                          :m-venue.spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                          :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                                          :m-venue.spec/style     :1}
                      :m-venue.spec/tiles
                                         [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                           :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                           :m-venue.spec/style :1}
                                          {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                           :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                           :m-venue.spec/style :2
                                           :m-venue.spec/img   2}
                                          correct-tile
                                          ]})

(deftest testing-correct-data
  (testing "Correct label"
    (let [serialized-label (tf/ser-map :m-venue.spec/label correct-label)]
      (println (str "serialized label: " serialized-label))
      (println (str "deserialized label: " (tf/de-ser-vector :m-venue.spec/label serialized-label))))
    (is (true? (s/valid? :m-venue.spec/label correct-label))))
  (testing "Correct tile"
    (let [serialized-title (tf/ser-map :m-venue.spec/tile correct-tile)]
      (println (str "serialized tile: " serialized-title))
      (println (str "deserialized title: " (tf/de-ser-vector :m-venue.spec/tile serialized-title))))
    (println (str "serialized tile: " (tf/ser-map :m-venue.spec/tile correct-tile)))
    (is (true? (s/valid? :m-venue.spec/tile correct-tile))))
  (testing "Correct gen-doc"
    (let [serialized-gen-doc (tf/ser-map :m-venue.spec/gen-doc correct-gen-doc)]
      (println (str "serialized gen-doc: " serialized-gen-doc))
      (println (str "deserialized gen-doc: " (tf/de-ser-vector :m-venue.spec/gen-doc serialized-gen-doc))))
    (is (true? (s/valid? :m-venue.spec/gen-doc correct-gen-doc)))))


