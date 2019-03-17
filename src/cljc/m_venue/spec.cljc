(ns m-venue.spec
  (:require [clojure.spec.alpha :as s]))

(def html (s/and (s/spec string?) #(> (count %) 10)))
(def label (s/and (s/spec string?) #(> (count %) 2) #(< (count %) 50)))

(s/def ::language #{:nl :en :de})

(s/def ::text html)
(s/def ::title label)
(s/def ::sub-title label)
(s/def ::img (s/spec number?))
(s/def ::href (s/spec (s/and (s/spec string?) #(> (count %) 10))))
(s/def ::style #{:0 :1 :2 :3 :4 :5})
(s/def ::content (s/keys :req [::title ::text] :opt [::sub-title]))
(s/def ::texts (s/and (s/spec map?) (s/every-kv ::language ::content)))
(s/def ::tile (s/keys :req [::texts ::style] :opt [::img ::href]))
(s/def ::tiles (s/and (s/spec vector?) (s/every ::tile)))

(s/def ::image-list (s/and (s/spec vector?) (s/every ::img)))

(s/def ::gen-doc (s/keys :req [::tile ::tiles]))
(s/def ::img-doc (s/keys :req [::tile ::image-list]))

(s/def ::base-64-square (s/spec string?))
(s/def ::base-64 (s/spec string?))
(s/def ::x-size (s/spec number?))
(s/def ::y-size (s/spec number?))
(s/def ::img-css-class #{"is-3by1" "is-2by1" "is-16by9" "is-5by3" "is-3by2" "is-4by3" "is-5by4" "is-1by1" "is-4by5" "is-3by4" "is-2by3" "is-3by5" "is-9by16" "is-1by2" "is-1by3"})
(s/def ::base-path (s/spec string?))
(s/def ::alt (s/spec label))
(s/def ::img-meta-info (s/keys :req [::title ::alt]))
(s/def ::img-meta-infos (s/and (s/spec map?) (s/every-kv ::language ::img-meta-info)))
(s/def ::img-reference (s/keys :req [::x-size ::y-size ::img-css-class ::base-path ::base-64 ::base-64-square ::img-meta-infos]))

(s/def ::img-path (s/spec string?))
(s/def ::latest-img (s/spec integer?))
(s/def ::img-uploaded-timestamp (s/spec number?))
(s/def ::img-summary (s/keys :req [::img-uploaded-timestamp ::base-path]))
(s/def ::img-summaries (s/and (s/spec map?) (s/every-kv keyword? ::img-summary)))
(s/def ::all-images (s/keys :req [::img-summaries ::img-path ::latest-img]))

(s/def ::n-title label)
(s/def ::p-reference label)
(s/def ::mdi-reference label)
(s/def ::nav-children (s/and (s/spec vector?) (s/every ::nav-item)))
(s/def ::nav-item (s/keys :req [::n-title (or ::p-reference ::href)] :opt [::mdi-reference ::nav-children]))

(s/def ::menu-summaries (s/and (s/spec map?) (s/every-kv keyword? ::language)))
(s/def ::all-menus (s/keys :req [::menu-summaries]))

(s/def ::doc-type #{:gen-doc :img-doc})
(s/def ::new-page (s/keys :req [::p-reference ::doc-type]))

(s/def ::page-created-timestamp (s/spec number?))
(s/def ::page-modified-timestamp (s/spec number?))
(s/def ::page-summary (s/keys :req [::page-created-timestamp ::page-modified-timestamp]))
(s/def ::page-summaries (s/and (s/spec map?) (s/every-kv keyword? ::page-summary)))
(s/def ::all-pages (s/keys :req [::page-summaries]))

(s/def ::ref-list (s/and (s/spec vector?) (s/every ::p-reference)))
(s/def ::side-content (s/keys :req [::ref-list]))

(s/def ::username label)
(s/def ::context (s/keys :req [::language ::username]))