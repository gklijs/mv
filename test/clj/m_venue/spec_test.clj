(ns m-venue.spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.spec :refer :all]))

(def correct-label {:m-venue.spec/nl-label "Katten"})
(def correct-tile {:m-venue.spec/title {:m-venue.spec/nl-label "Blokje over katten"}
                   :m-venue.spec/text {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                   :m-venue.spec/img "sjors.jpg"})
(def correct-gen-doc {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over katten"}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text {:m-venue.spec/nl-text "Een mogelijk erg lange text
                                            over het voeren van katten."}}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            :m-venue.spec/text {:m-venue.spec/nl-text "Een mogelijk erg lange text
                                            over speeltjes voor katten."}
                                            :m-venue.spec/img "uil.jpg"}
                                           ]})

(deftest testing-correct-data
  (testing "Correct label"
    (is (true? (s/valid? :m-venue.spec/label correct-label))))
  (testing "Correct tile"
    (is (true? (s/valid? :m-venue.spec/tile correct-tile))))
  (testing "Correct gen-doc"
    (println (s/explain :m-venue.spec/gen-doc correct-gen-doc))
    (is (true? (s/valid? :m-venue.spec/gen-doc correct-gen-doc)))))


