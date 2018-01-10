(ns m-venue.admin-spec
  (:require [clojure.spec.alpha :as s]
            [m-venue.spec :refer [label]]))

(def editor #{"editor" "admin"})

(defn is-editor
  [profile]
  (contains? editor (::role profile)))

(s/def ::username label)
(s/def ::password (s/spec string?))
(s/def ::email label)
(s/def ::role #{"user" "editor" "admin"})
(s/def ::profile (s/keys :req [::username ::password ::email ::role]))

