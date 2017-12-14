(ns m-venue.users-test

  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]
            [m-venue.admin-spec :as admin-spec]))

(def correct-profile {::admin-spec/username "Gerard Klijs"
                      ::admin-spec/email    "g.klijs@gmail.com"
                      ::admin-spec/password "test1234"
                      ::admin-spec/role     "admin"})

(def correct-profile2 {::admin-spec/username "Martha Huijser"
                      ::admin-spec/email    "teigetje77@gmail.com"
                      ::admin-spec/password "test1234"
                      ::admin-spec/role     "editor"})

(deftest profile-test
  (testing "set"
    (is (s/valid? ::admin-spec/profile correct-profile))
    (is (not (nil? (repo/set-map! ::ad-spec/profile "gklijs" correct-profile))))
    (is (not (nil? (repo/set-map! ::ad-spec/profile "mhuijser" correct-profile2)))))
  (testing "get"
    (is (= correct-profile (second (repo/get-map :u "gklijs"))))
    (is (= correct-profile2 (second (repo/get-map :u "mhuijser"))))))


