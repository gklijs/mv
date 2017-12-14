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

(defn all-for-conn
  ([function t conn] (all-for-conn function t conn 0))
  ([function t conn cursor]
   (log/debug "all-for-conn called" t conn cursor "first" (car/wcar conn (car/scan cursor)))
   (let [[new-cursor key-list] (car/wcar conn (car/scan cursor))
         value-list (car/wcar conn (apply car/mget key-list))
         k-v-map (zipmap key-list value-list)]
     (doseq [[k v] k-v-map] #(function t k (str v)))
     (if (= "0" new-cursor) true (recur function t conn new-cursor))
     )))

(defn for-all
  [function]
  (all-for-conn function "n" n-conn)
  (all-for-conn function "i" i-conn)
  (all-for-conn function "p" p-conn))

(defn get-profile
  [user-id]
  (if-let [result (car/wcar u-conn (car/get user-id))]
    (s-core/de-ser-vector (first result) (second result))))
