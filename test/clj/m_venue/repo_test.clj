(ns m-venue.repo-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]
            [m-venue.spec :as spec]))

(def correct-gen-doc {:m-venue.spec/tile  {::spec/title     {:m-venue.spec/nl-label "Alles over katten"}
                                           ::spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                           ::spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                                           ::spec/style     :1}
                      :m-venue.spec/tiles [{::spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            ::spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                            ::spec/style :4
                                            ::spec/href  "http://www.nu.nl"}
                                           {::spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            ::spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                            ::spec/img   1
                                            ::spec/style :3}]})

(def nav-items {::spec/n-title     "Nederlands menu"
                ::spec/p-reference "wordt niet gebruikt"
                ::spec/nav-children
                                   [{::spec/n-title       "Katten"
                                     ::spec/p-reference   "cats"
                                     ::spec/mdi-reference "cat"
                                     ::spec/nav-children
                                                                             [{::spec/n-title     "Sjors"}]
                                                                               ::spec/p-reference "sjors"
                                                                              {::spec/n-title     "Saar"}
                                                                               ::spec/p-reference "saar"
                                                                              {::spec/n-title     "Amber"}
                                                                               ::spec/p-reference "amber"}
                                    {::spec/n-title "Google"
                                     ::spec/href    "http://www.google.com"}
                                    {::spec/n-title       "Info"
                                     ::spec/p-reference   "info"
                                     ::spec/mdi-reference "information-outline"}]})

(def prod-img-info {::spec/img-path   "public/img/"
                    ::spec/latest-img 0})

(def correct-side-content {::spec/ref-list ["sjors" "saar" "amber"]})

(deftest repo-test
  (testing "set-gen-doc"
    (is (nil? (repo/set-map! :p "test-home" :m-venue.spec/gen-doc correct-gen-doc)))
    (let [result (repo/set-map! :mvp "xxxx" :m-venue.spec/label correct-gen-doc)]
      (is (map? result))
      (is (seq? (::s/problems result)))))
  (testing "get-gen-doc"
    (s/explain :m-venue.spec/gen-doc correct-gen-doc)
    (println (str "result from repo: " (repo/get-map :p "test-home")))
    (is (= :m-venue.spec/gen-doc (first (repo/get-map :p "test-home"))))
    (is (= correct-gen-doc (second (repo/get-map :p "test-home"))))
    (is (nil? (repo/get-map :p "xxxx")))
    (repo/remove-key! :p "test-home")
    (is (nil? (repo/get-map :p "test-home"))))
  (testing "set-nav"
    (is (nil? (repo/set-map! :n "test" ::spec/nav-item nav-items))))
  (testing "get-nav"
    (s/explain ::spec/nav-item nav-items)
    (println (str "result from repo: " (repo/get-map :n "test")))
    (println (repo/get-string :n "test"))
    (is (= ::spec/nav-item (first (repo/get-map :n "test"))))
    (is (= nav-items (second (repo/get-map :n "test"))))
    (repo/remove-key! :n "test")
    (is (nil? (repo/get-map :n "test")))))

(deftest initial-menu
  (testing "set-inital-menu"
    (repo/set-map! :n "main-nl" ::spec/nav-item nav-items)
    (println (repo/get-string :n "main-nl"))
    (is (= ::spec/nav-item (first (repo/get-map :n "main-nl"))))
    (is (= nav-items (second (repo/get-map :n "main-nl"))))))

(deftest initial-cats
  (testing "set-inital-cats"
    (repo/set-map! :p "sjors" ::spec/gen-doc correct-gen-doc)
    (repo/set-map! :p "saar" ::spec/gen-doc correct-gen-doc)
    (repo/set-map! :p "amber" ::spec/gen-doc correct-gen-doc)))

(deftest initial-side-content
  (testing "set-initial-side-content"
    (repo/set-map! :n "side-nl" ::spec/side-content correct-side-content)))

(deftest set-prod-img-info
  (testing "set-prod-img-info"
    (repo/set-map! :i "info" ::spec/img-info prod-img-info)))