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

(s/def ::profile-created-timestamp (s/spec number?))
(s/def ::last-login-timestamp (s/spec number?))
(s/def ::profile-key (s/spec string?))
(s/def ::profile-summary (s/keys :req [::profile-created-timestamp ::last-login-timestamp]))
(s/def ::profile-summaries (s/and (s/spec map?) (s/every-kv keyword? ::profile-summary)))
(s/def ::all-profiles (s/keys :req [::profile-summaries]))
