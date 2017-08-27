(ns m-venue.repo-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]))

(def correct-gen-doc {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over katten"}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                            :m-venue.spec/style :4
                                            :m-venue.spec/href "http://www.nu.nl"}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                            :m-venue.spec/img   "uil.jpg"
                                            :m-venue.spec/style :3}
                                           ]})

(deftest repo-test
  (testing "get-spec"
    (is (keyword? (repo/get-spec "mh-gd-home")))
    (is (nil? (repo/get-spec "mh-xx-xxxx"))))
  (testing "set"
    (is (map? (repo/set-map "mh-gd-home" correct-gen-doc)))
    (is (nil? (repo/set-map "mh-xx-xxxx" correct-gen-doc))))
  (testing "get"
    (s/explain :m-venue.spec/gen-doc correct-gen-doc)
    (println (str "result from repo: " (repo/get-map "mh-gd-home")))
    (is (= correct-gen-doc (repo/get-map "mh-gd-home")))
    (is (nil? (repo/get-map "mh-xx-xxxx")))))


