(ns m-venue.repo-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]))

(def correct-gen-doc {:m-venue.spec/tile  {:m-venue.spec/title     {:m-venue.spec/nl-label "Alles over katten"}
                                           :m-venue.spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                           :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                                           :m-venue.spec/style     :1}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                            :m-venue.spec/style :4
                                            :m-venue.spec/href  "http://www.nu.nl"}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                            :m-venue.spec/img   "uil.jpg"
                                            :m-venue.spec/style :3}
                                           ]})

(deftest repo-test
  (testing "set"
    (is (map? (repo/set-map! "mvp-home" :m-venue.spec/gen-doc correct-gen-doc)))
    (let [result (repo/set-map! "mvp-xxxx" :m-venue.spec/label correct-gen-doc)]
      (is (map? result))
      (is (seq? (::s/problems result)))
      ))
  (testing "get"
    (s/explain :m-venue.spec/gen-doc correct-gen-doc)
    (println (str "result from repo: " (repo/get-map "mvp-home")))
    (is (= :m-venue.spec/gen-doc (first (repo/get-map "mvp-home"))))
    (is (= correct-gen-doc (second (repo/get-map "mvp-home"))))
    (is (nil? (repo/get-map "mvp-xxxx")))))


