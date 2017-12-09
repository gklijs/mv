(ns m-venue.spec
  (:require [clojure.spec.alpha :as s]))

(def html (s/and (s/spec string?) #(> (count %) 20)))
(def label (s/and (s/spec string?) #(> (count %) 2) #(< (count %) 40)))

(s/def ::nl-label label)
(s/def ::label (s/keys :req [::nl-label]))
(s/def ::nl-text html)
(s/def ::text (s/keys :req [::nl-text]))
(s/def ::title (s/spec ::label))
(s/def ::sub-title (s/spec ::label))
(s/def ::img (s/spec number?))
(s/def ::href (s/spec (s/and (s/spec string?) #(> (count %) 10))))
(s/def ::style #{:0 :1 :2 :3 :4 :5})
(s/def ::tile (s/keys :req [::title ::text ::style] :opt [::sub-title ::img ::href]))
(s/def ::tiles (s/and (s/spec vector?) (s/every ::tile)))

(s/def ::gen-doc (s/keys :req [::tile ::tiles]))

(s/def ::img-path (s/spec string?))
(s/def ::latest-img (s/spec number?))
(s/def ::img-info (s/keys :req [::img-path ::latest-img]))

(s/def ::x-size (s/spec number?))
(s/def ::y-size (s/spec number?))
(s/def ::img-css-class #{"is-2by1" "is-16by9" "is-3by2" "is-4by3" "is-1by1" "is-3by4" "is-2by3" "is-9-by-16" "is-1by2"})
(s/def ::base-path (s/spec string?))
(s/def ::alt (s/spec ::label))
(s/def ::img-reference (s/keys :req [::x-size ::y-size ::img-css-class ::base-path] :opt [::title ::alt]))

(s/def ::n-title label)
(s/def ::p-reference label)
(s/def ::mdi-reference label)
(s/def ::nav-children (s/and (s/spec vector?) (s/every ::nav-item)))
(s/def ::nav-item (s/keys :req [::n-title (or ::p-reference ::href)] :opt [::mdi-reference ::nav-children]))

(s/def ::doc-type #{:gen-doc})
(s/def ::new-page (s/keys :req [::p-reference ::doc-type]))

