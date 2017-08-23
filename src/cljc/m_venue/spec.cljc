(ns m-venue.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::nl-label (s/and (s/spec string?) #(> (count %) 3) #(<  (count %) 30)))
(s/def ::label (s/keys :req [::nl-label]))
(s/def ::nl-text (s/and (s/spec string?) #(> (count %) 20)))
(s/def ::text (s/keys :req [::nl-text]))
(s/def ::title (s/spec ::label))
(s/def ::img (s/and (s/spec string?) #(re-matches #"^[a-z]{2,20}.jpg" %)))

(s/def ::tile (s/keys :req [::title ::text] :opt [::img]))
(s/def ::tiles (s/and (s/spec vector?) (s/every ::tile) #(> (count %) 1 )))

(s/def ::gen-doc (s/keys :req [::title ::tiles]))
