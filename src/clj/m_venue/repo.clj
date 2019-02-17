(ns m-venue.repo
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [m-venue.env-config :refer [file-map]]
            [spec-serialize.core :refer [de-ser-vector ser-map]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn- keys->path
  [k key]
  (str (k file-map) (name key) ".edn"))

(defn- keys->map
  "fetches the vector from a part, and turns is into a map"
  [k key]
  (let [full-path (keys->path k key)
        [spec vec-values] (edn/read-string (slurp full-path))]
    [spec (de-ser-vector spec vec-values)]))

(defn- add-reference
  [k map reference]
  (let [reference-map (keys->map k reference)]
    (assoc map reference reference-map)))

(defn- init-data
  "get the initial data from the file system"
  [k]
  (let [[spec-summary map-summary] (keys->map k :summary)
        data {:summary [spec-summary map-summary]}]
    (reduce (partial add-reference k) data (keys (second (first map-summary))))))

(defn- spit-map
  "serializes and spits a value from the map"
  [k key map]
  (if-let [[spec data] (key map)]
    (spit (keys->path k key) [spec (ser-map spec data)])))

(defn- file-sync
  "function to sync files on changes to a map"
  [k watch-key r os ns]
  (log/debug "file-sync called with" k watch-key r os ns)
  (let [old-keys (set (keys os))
        new-keys (set (keys ns))
        removed (clojure.set/difference old-keys new-keys)
        added (clojure.set/difference new-keys old-keys)
        kept (clojure.set/intersection old-keys new-keys)]
    (doseq [r removed] (io/delete-file (keys->path k r)))
    (doseq [a added] (spit-map k a ns))
    (doseq [a kept] (if (not (= (get os a) (get ns a))) (spit-map k a ns)))))

(defn- set-data
  "create atom with subscription"
  [k]
  (let [data-atom (atom (init-data k))
        _ (add-watch data-atom :file-sync (partial file-sync k))]
    [k data-atom]))

(defonce data-atoms (into {} (map set-data (keys file-map))))

(defn- to-key
  [k]
  (if (number? k)
    (keyword (str k))
    (keyword k)))

(defn- with-keys
  [k key f]
  (if-let [k-t (to-key k)]
    (if-let [key-t (to-key key)]
      (f k-t key-t))))

(defn- set-content!
  [k key data]
  (with-keys k key #(if-let [data-atom (%1 data-atoms)]
                      (%2 (swap! data-atom assoc %2 data))
                      (log/debug "set-content failed because k" %1 "is not valid, there is no map. Valid keys are" (keys data-atoms)))))

(defn- update-if-still-valid
  [f [spec map]]
  (let [new-map (f map)]
    (if (s/valid? spec new-map)
    [spec new-map]
    [spec map])))

(defn update-map!
  [k key f]
  (with-keys k key #(if-let [data-atom (%1 data-atoms)]
                      (second (%2 (swap! data-atom update %2 (fn [val] (update-if-still-valid f val)))))
                      (log/debug "update-content failed because k" %1 "is not valid, there is no map. Valid keys are" (keys data-atoms)))))

(defn set-map!
  "validate and add/overwrite item in repo"
  [k key spec map]
  (if
    (s/valid? spec map)
    (set-content! k key [spec map])
    (log/debug "set-map! failed with k" k "key" key "spec" spec "and map" map "because" (s/explain-data spec map))))

(defn set-string!
  "validate and add/overwrite item in repo"
  [k key data]
  (if-let [[spec vec-value] (read-string data)]
    (let [map (de-ser-vector spec vec-value)]
      (if (s/valid? spec map)
        (set-content! k key [spec map])
        (log/debug "set-string! failed with key" key "spec" spec "and data" data "because" (s/explain-data spec data))))
    (log/debug "set-string! failed because data" data "could not be read to be [spec vec-value]")))

(defn get-map
  [k key]
  (with-keys k key #(if-let [data-atom (%1 data-atoms)]
                      (if-let [result (%2 @data-atom)]
                        result
                        (log/debug "no result from data-atom for key" key "valid keys are" (keys @data-atom)))
                      (log/debug "could not get conn because invalid k value:" k "valid keys are" (keys data-atoms)))))

(defn get-string
  [k key]
  (with-keys k key #(if-let [data-atom (%1 data-atoms)]
                      (if-let [[spec map] (%2 @data-atom)]
                        (str [spec (ser-map spec map)])
                        (log/debug "could not get data because not present:" key "valid keys are" (keys @data-atom)))
                      (log/debug "could not get data because no valid k value:" k "valid keys are" (keys data-atoms)))))

(defn all-for-conn
  [function k]
  (if-let [data-atom (k data-atoms)]
    (doseq [[key value] @data-atom] (function k key value))
    (log/debug "could not do all-for-conn because no valid k value:" k "valid keys are" (keys data-atoms))))

(defn for-all
  [function]
  (all-for-conn function :n)
  (all-for-conn function :i)
  (all-for-conn function :p))

(defn remove-key!
  [k key]
  (with-keys k key #(if-let [data-atom (%1 data-atoms)]
                      (swap! data-atom (fn [data]
                                         (-> data
                                             (dissoc %2)
                                             (update-in [:summary 1 0 1] dissoc %2))))
                      (log/debug "could not get data because no valid k value: " k "valid keys are" (keys data-atoms)))))