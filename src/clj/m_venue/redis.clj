(ns m-venue.redis
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [m-venue.admin-spec :as ad-spec]
            [m-venue.spec]
            [m-venue.redis-connection :refer [i-conn p-conn n-conn u-conn]]
            [taoensso.carmine :as car :refer (wcar)]
            [spec-serialize.core :as s-core]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]))

(defonce conn-map {"i" i-conn "p" p-conn "n" n-conn "u" u-conn})

(defn set-content
  [key data]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (if-let [conn (get conn-map type)]
      (car/wcar conn (car/set specifier (read-string data))))
    (log/debug "could not set data: " data " because invalid key: " key)))

(defn get-content
  [key]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (if-let [conn (get conn-map type)]
      (if-let [result (car/wcar conn (car/get specifier))]
        (str result))
      (log/debug "could not get data because invalid key: " key))))

(defn get-map
  [key]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (if-let [conn (get conn-map type)]
      (if-let [result (car/wcar conn (car/get specifier))]
        [(first result) (s-core/de-ser-vector (first result) (second result))]
        (log/debug "no result from redis for key " key))
      (log/debug "could not get conn because invalid key: " key))
    (log/debug "incorrect key: " key)))

(defn remove-key
  [key]
  (if-let [[type specifier] (string/split key #"-" 2)]
    (if-let [conn (get conn-map type)]
      (car/wcar conn (car/del specifier)))
    (log/debug "could not get data because invalid key: " key)))

(defn set-profile
  [user-id profile]
  (if
    (s/valid? ::ad-spec/profile profile)
    (let [serialized-profile (s-core/ser-map ::ad-spec/profile profile)]
      (car/wcar u-conn (car/set user-id serialized-profile)))
    (log/debug "could not set profile: " profile " because invalid according to spec")))

(defn for-all
  [function]
  (log/debug "function: " function "not called, because not implemented yet"))

(defn get-profile
  [user-id]
  (if-let [result (car/wcar u-conn (car/get user-id))]
    (s-core/de-ser-vector (first result) (second result))))
