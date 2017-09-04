(ns m-venue.mc
  (:require [clojure.string :as string]
            [cljs.reader]
            [m-venue.websocket :refer [send-msg! subscribe]]
            [spec-serialize.impl :as tf]))

(defn init!
  "Initializes the handlers"
  []
  (subscribe (fn [msg] (string/starts-with? msg "mc-")) (fn [msg] (println (tf/de-ser-string :m-venue.spec/label (subs msg 3))))))

