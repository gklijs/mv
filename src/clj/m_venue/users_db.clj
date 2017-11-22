(ns m-venue.users-db
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [m-venue.admin-spec :as admin-spec]
            [spec-serialize.impl :refer [from-string to-string]])
  (:import (org.h2.mvstore MVStore)))

(defonce content-store (MVStore/open "users.db"))

(defn get-content-map
  ([user-id] (get-content-map user-id true))
  ([user-id create-if-not-exist]
   (if
     (or create-if-not-exist (.hasMap content-store user-id))
     (.openMap content-store user-id))))

(defn set-profile
  [user-id profile]
  (if
    (s/valid? ::admin-spec/profile profile)
    (let [content-map (get-content-map user-id)
          serialized-profile (to-string ::admin-spec/profile profile)]
      (.put content-map "profile" serialized-profile))
    (log/debug "could not set profile: " profile " because invalid according to spec")))

(defn get-profile
  [user-id]
  (if-let [content-map (get-content-map user-id false)]
    (if-let [serialized-profile (.get content-map "profile")]
      (second (from-string serialized-profile)))))
