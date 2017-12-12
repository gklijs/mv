(ns m-venue.users-db-test

  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.redis :as redis]
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
    (is (not (nil? (redis/set-profile "gklijs" correct-profile))))
    (is (not (nil? (redis/set-profile "mhuijser" correct-profile2)))))
  (testing "get"
    (is (= correct-profile (redis/get-profile "gklijs")))
    (is (= correct-profile2 (redis/get-profile "mhuijser")))))


