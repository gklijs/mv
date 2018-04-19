(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [m-venue.redis-connection :refer [i-conn p-conn n-conn u-conn]]
            [spec-serialize.core :refer [de-ser-vector de-ser-vector ser-map]]
            [taoensso.carmine :as car :refer (wcar)]))

(defonce conn-map {:i i-conn :p p-conn :n n-conn :u u-conn})

(defn set-content!
  ([t-k data]
   (if-let [[conn-type key] (string/split t-k #"-" 2)]
     (set-content! (keyword conn-type) key data)
     (log/debug "could not set data:" data "because invalid key:" key)))
  ([conn-type key data]
   (if-let [conn (conn-type conn-map)]
     (car/wcar conn (car/set key data)))))

(defn set-map!
  "validate and add/overwrite item in repo"
  [key spec data]
  (if
    (s/valid? spec data)
    (set-content! key [spec (ser-map spec data)])
    (log/debug "set-map! failed with key" key "spec" spec "and data" data "because" (s/explain-data spec data))))

(defn set-string!
  "validate and add/overwrite item in repo"
  [key data]
  (if-let [[spec vec-value] (read-string data)]
    (let [map-value (de-ser-vector spec vec-value)]
      (if (s/valid? spec map-value)
        (set-content! key [spec vec-value])
        (log/debug "set-string! failed with key" key "spec" spec "and data" data "because" (s/explain-data spec data))))
    (log/debug "set-string! failed because data" data "could not be read to be [spec vec-value]")))

(defn get-map
  [conn-type key]
  (if-let [conn (conn-type conn-map)]
    (if-let [result (car/wcar conn (car/get key))]
      [(first result) (de-ser-vector (first result) (second result))]
      (log/debug "no result from redis for key" key "used conn" conn))
    (log/debug "could not get conn because invalid conn-type:" conn-type)))

(defn get-string
  [t-k]
  (if-let [[conn-type key] (string/split t-k #"-" 2)]
    (if-let [conn ((keyword conn-type) conn-map)]
      (if-let [result (car/wcar conn (car/get key))]
        (str result)
        (log/debug "could not get data because not present:" key))
      (log/debug "could not get data because no valid type:" conn-type))
    (log/debug "set-string! failed because key" key "could not be split to be [spec vec-value]")))

(defn all-for-conn
  ([function t] (all-for-conn function t 0))
  ([function t cursor]
   (let [conn (t conn-map)
         [new-cursor key-list] (car/wcar conn (car/scan cursor))
         value-list (car/wcar conn (apply car/mget key-list))
         k-v-map (zipmap key-list value-list)]
     (doseq [[k v] k-v-map] #(function t k v))
     (if (= "0" new-cursor) true (recur function t new-cursor)))))

(defn for-all
  [function]
  (all-for-conn function :n)
  (all-for-conn function :i)
  (all-for-conn function :p))

(defn remove-key
  [conn-type key]
  (if-let [conn (conn-type conn-map)]
    (car/wcar conn (car/del key)))
  (log/debug "could not get data because invalid conn-type: " conn-type))