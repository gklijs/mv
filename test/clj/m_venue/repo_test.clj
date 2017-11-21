(ns m-venue.repo-test
  (:import org.h2.mvstore.MVStore
           (org.h2.mvstore MVMap))
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]
            [m-venue.spec :as spec]))

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
                                            :m-venue.spec/img   1
                                            :m-venue.spec/style :3}
                                           ]})

(deftest repo-test
  (testing "set"
    (is (nil? (repo/set-map! "mvp-home" :m-venue.spec/gen-doc correct-gen-doc)))
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

(deftest test-mvstore
  (testing "something"
    (let [mvstore (MVStore/open "test.db")
          mvmap (.openMap mvstore "test")
          set-something (.put mvmap "123" "456")
          set-something2 (.put mvmap "1232" "4562")
          set-something2 (.put mvmap "🇲🇼" "🇹🇬✜")]
      (is (= 3 (.size mvmap)))
      (is (= "456" (.get mvmap "123")))
      (is (= "4562" (.get mvmap "1232")))
      (is (= "🇹🇬✜" (.get mvmap "🇲🇼")))
      (.commit mvstore)
      (let [mvmap (.openMap mvstore "test2")
            set-something (.put mvmap "123-" "456")
            set-something2 (.put mvmap "🇲🇼-" "🇹🇬✜")]
        (is (= 2 (.size mvmap)))
        (is (= "456" (.get mvmap "123-")))
        (is (= "🇹🇬✜" (.get mvmap "🇲🇼-")))
        (.commit mvstore)))))


