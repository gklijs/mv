(ns m-venue.edit-app
  (:require [m-venue.editable :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)