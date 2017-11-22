(ns m-venue.admin-spec
  (:require [clojure.spec.alpha :as s]
            [m-venue.spec :refer [label]]))

(s/def ::username label)
(s/def ::password label)
(s/def ::email label)
(s/def ::role #{"user" "editor" "admin"})
(s/def ::profile (s/keys :req [::username ::password ::email ::role]))

