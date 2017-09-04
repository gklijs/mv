(ns m-venue.spec
  (:require [#?(:clj     clojure.spec.alpha
                :cljs    cljs.spec.alpha
                :default clojure.spec.alpha) :as s]))

(s/def ::nl-label (s/and (s/spec string?) #(> (count %) 3) #(< (count %) 40)))
(s/def ::label (s/keys :req [::nl-label]))
(s/def ::nl-text (s/and (s/spec string?) #(> (count %) 20)))
(s/def ::text (s/keys :req [::nl-text]))
(s/def ::title (s/spec ::label))
(s/def ::sub-title (s/spec ::label))

(s/def ::img (s/and (s/spec string?) #(re-matches #"^[a-z]{2,20}.jpg" %)))

(s/def ::date inst?)
(s/def ::href (s/spec string?))
(s/def ::style #{:0 :1 :2 :3 :4 :5})

(s/def ::tile (s/keys :req [::title ::text ::style] :opt [::sub-title ::img ::href]))
(s/def ::tiles (s/and (s/spec vector?) (s/every ::tile) #(> (count %) 1)))

(s/def ::gen-doc (s/keys :req [::tile ::tiles]))
