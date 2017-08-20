(ns m-venue.app
  (:require [m-venue.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
