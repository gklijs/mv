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

(def nav-items {::spec/n-title "Nederlands menu"
                ::spec/p-reference "wordt niet gebruikt"
                ::spec/nav-children
                [
                 {::spec/n-title "Katten"
                  ::spec/p-reference "cats"
                  ::spec/mdi-reference "cat"
                  ::spec/nav-children
                  [
                   {::spec/n-title "Sjors"
                    ::spec/p-reference "sjors"}
                   {::spec/n-title "Saar"
                    ::spec/p-reference "saar"}
                   {::spec/n-title "Amber"
                    ::spec/p-reference "amber"}
                   ]}
                 {::spec/n-title "Google"
                  ::spec/href "http://www.google.com"}
                 {::spec/n-title "Info"
                  ::spec/p-reference "info"
                  ::spec/mdi-reference "information-outline"}
                 ]})

(def prod-img-info {:m-venue.spec/img-path   "public/img/"
                    :m-venue.spec/latest-img 5})

(deftest repo-test
  (testing "set-gen-doc"
    (is (nil? (repo/set-map! "p-test-home" :m-venue.spec/gen-doc correct-gen-doc)))
    (let [result (repo/set-map! "mvp-xxxx" :m-venue.spec/label correct-gen-doc)]
      (is (map? result))
      (is (seq? (::s/problems result)))))
  (testing "get-gen-doc"
    (s/explain :m-venue.spec/gen-doc correct-gen-doc)
    (println (str "result from repo: " (repo/get-map "p-test-home")))
    (is (= :m-venue.spec/gen-doc (first (repo/get-map "p-test-home"))))
    (is (= correct-gen-doc (second (repo/get-map "p-test-home"))))
    (is (nil? (repo/get-map "mvp-xxxx")))
    (repo/remove-key "p-test-home")
    (is (nil? (repo/get-map "p-test-home")))
    (repo/close))
  (testing "set-nav"
    (is (nil? (repo/set-map! "n-test" ::spec/nav-item nav-items))))
  (testing "get-nav"
    (s/explain ::spec/nav-item nav-items)
    (println (str "result from repo: " (repo/get-map "n-test")))
    (println (repo/get-string "n-test"))
    (is (= ::spec/nav-item (first (repo/get-map "n-test"))))
    (is (= nav-items (second (repo/get-map "n-test"))))
    (repo/remove-key "n-test")
    (is (nil? (repo/get-map "n-test")))
    (repo/close)))

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

(deftest initial-menu
  (testing "set-inital-menu"
    (repo/set-map! "n-main-nl" ::spec/nav-item nav-items)
    (println (repo/get-string "n-main-nl"))
    (is (= ::spec/nav-item (first (repo/get-map "n-main-nl"))))
    (is (= nav-items (second (repo/get-map "n-main-nl"))))
    (repo/close)
    (Thread/sleep 10000)))

(deftest initial-cats
  (testing "set-inital-cats"
    (repo/set-map! "p-sjors" :m-venue.spec/gen-doc correct-gen-doc)
    (repo/set-map! "p-saar" :m-venue.spec/gen-doc correct-gen-doc)
    (repo/set-map! "p-amber" :m-venue.spec/gen-doc correct-gen-doc)
    (repo/close)
    (Thread/sleep 10000)))

(deftest set-prod-img-info
  (testing "set-prod-img-info"
    (repo/set-map! "i-info" ::spec/img-info prod-img-info)
    (repo/close)
    (Thread/sleep 10000)))