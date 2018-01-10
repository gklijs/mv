(ns m-venue.users-test

  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [m-venue.repo :as repo]
            [m-venue.admin-spec :as admin-spec]))

(def correct-profile {::admin-spec/username "Gerard Klijs"
                      ::admin-spec/email    "g.klijs@gmail.com"
                      ::admin-spec/password "bcrypt+sha512$0a954502d5adab29144b959f9f0fe608$12$7017d30af672bbf14282b2de3dd912205293b4f24b9b8255"
                      ::admin-spec/role     "admin"})

(def correct-profile2 {::admin-spec/username "Martha Huijser"
                      ::admin-spec/email     "teigetje77@gmail.com"
                       ::admin-spec/password "bcrypt+sha512$c49b5851c99033a9b80accd841797898$12$942b7a28e6f1bc8cab5f46f50da8a0441cdb4c7ab004b81d"
                      ::admin-spec/role      "editor"})

(deftest profile-test
  (testing "set"
    (is (s/valid? ::admin-spec/profile correct-profile))
    (is (not (nil? (repo/set-map! "u-gklijs" ::admin-spec/profile correct-profile))))
    (is (not (nil? (repo/set-map! "u-mhuijser" ::admin-spec/profile correct-profile2)))))
  (testing "get"
    (is (= correct-profile (second (repo/get-map :u "gklijs"))))
    (is (= correct-profile2 (second (repo/get-map :u "mhuijser"))))))


