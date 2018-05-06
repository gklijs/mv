(ns m-venue.templates
  (:require [m-venue.constants :refer [image-sizes style-map relative-height-map]]
            [m-venue.repo :as repo]
            [m-venue.spec :as spec]))

(defn get-correct-image
  [size x-size]
  (if (> x-size (size image-sizes))
    size
    :s))

(defn small-square-img
  [id]
  [:img {:id (str "img-select-" id) :src (str "/img/" id "/64.jpg") :data-id (str "i-" id)}])

(defn all-images
  [latest]
  [:figure#all-images (map small-square-img (take latest (iterate dec latest)))])

(defn responsive-image
  [img-reference size]
  [:figure {:class (str "image " (::spec/img-css-class img-reference))}
   [:img {:src   (str (::spec/base-path img-reference) (name (get-correct-image size (::spec/x-size img-reference))) ".jpg")
          :title (get-in img-reference [::spec/title :m-venue.spec/nl-label])
          :alt   (get-in img-reference [::spec/alt :m-venue.spec/nl-label])}]])

(defn navbar-item
  [nav-item path parent-path]
  (let [has-children (and (nil? parent-path) (> (count (::spec/nav-children nav-item)) 0))
        matching-part (if (nil? parent-path) (first path) (second path))
        is-active (if (and matching-part (= matching-part (::spec/p-reference nav-item))) " is-active")
        title [:span (::spec/n-title nav-item)]
        icon (if-let [icon-class (::spec/mdi-reference nav-item)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])
        [href target] (if-let [url (::spec/href nav-item)]
                        [url "_blank"]
                        [(str parent-path "/" (::spec/p-reference nav-item)) "_self"])]
    (if has-children
      [:div.navbar-item.has-dropdown.is-hoverable
       [:a {:href href :target target :class (str "navbar-link" is-active)} icon title]
       [:div.navbar-dropdown
        (map #(navbar-item % path href) (::spec/nav-children nav-item))]]
      [:a {:href href :target target :class (str "navbar-item is-tab" is-active)} icon title])))

(defn flex-main-menu
  [path nav-item]
  (if-let [navbar-items (::spec/nav-children nav-item)]
    [:div#flex-main-menu.navbar-start
     (map #(navbar-item % path nil) navbar-items)]))

(defn nav-bar
  [path side-menu]
  [:nav#nav-bar.navbar.is-fixed-top {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    (if side-menu
      [:button#burger-side-content.button.navbar-burger.is-hidden-tablet
       [:span]
       [:span]
       [:span]])
    [:a.navbar-item.is-tab
     {:href "/" :class (if (or (= nil path) (= ["home"] path)) "is-active" "")}
     [:span.is-large "Martha's Venue"]]
    [:a.navbar-item.is-hidden-desktop
     {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
     [:span.icon {:style "color: #4267b2;"} [:i.mdi.mdi-24px.mdi-facebook]]]
    [:button#burger-menu.button.navbar-burger
     [:span]
     [:span]
     [:span]]]
   [:div#main-menu.navbar-menu
    (flex-main-menu path (second (repo/get-map :n "main-nl")))
    [:div.navbar-end
     [:a.navbar-item.is-hidden-touch
      {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
      [:span.icon {:style "color: #4267b2;"} [:i.mdi.mdi-24px.mdi-facebook]]]]]
   (if side-menu
     [:div#side-content.is-hidden side-menu])])

(defn side-menu-item
  [base-path nav-item selected]
  (let [is-active (if (= selected (::spec/p-reference nav-item)) " is-active")
        icon (if-let [icon-class (::spec/mdi-reference nav-item)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])
        [href target] (if-let [url (::spec/href nav-item)]
                        [url "_blank"]
                        [(str base-path "/" (::spec/p-reference nav-item)) "_self"])]
    [:li [:a {:class is-active :href href :target target} icon (::spec/n-title nav-item)]
     (if (> (count (::spec/nav-children nav-item)) 0)
       [:ul.menu-list (map #(side-menu-item href % selected) (::spec/nav-children nav-item))])]))

(defn side-menu
  [level-two-child path]
  (let [base-path (str "/" (first path) "/" (second path))
        icon (if-let [icon-class (::spec/mdi-reference level-two-child)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])]
    [:aside#side-menu.menu
     [:a.menu-label {:href base-path} icon (::spec/n-title level-two-child)]
     [:ul.menu-list (map #(side-menu-item base-path % (last path)) (::spec/nav-children level-two-child))]]))

(defn side-menu?
  [path nav-item]
  (if-let [level-one-items (::spec/nav-children nav-item)]
    (if-let [level-one-child (first (filter #(= (first path) (::spec/p-reference %)) level-one-items))]
      (let [level-two-items (::spec/nav-children level-one-child)
            level-two-part (second path)]
        (if-let [level-two-child (first (filter #(= level-two-part (::spec/p-reference %)) level-two-items))]
          (if (> (count (::spec/nav-children level-two-child)) 0)
            (side-menu level-two-child path)))))))

(defn footer
  "renders a footer"
  []
  [:footer#footer.footer
   [:div.container
    [:div.content.has-text-centered
     [:p
      [:strong "Bulma"]
      " by "
      [:a {:href "http://jgthms.com"} "Jeremy Thomas"]
      ". The source code is licensed\n        "
      [:a
       {:href "http://opensource.org/licenses/mit-license.php"}
       "MIT"]
      ". The website content\n        is licensed "
      [:a
       {:href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
       "CC ANS 4.0"]
      ".\n      "]
     [:p
      [:a.icon
       {:href "https://github.com/jgthms/bulma"}
       [:i.mdi.mdi-message]]]]]])

(defn s-content
  ([] (s-content nil))
  ([path] (s-content path nil))
  ([path first-item]
   (let [side-content (second (repo/get-map :n "side-nl"))
         last (if (vector? path) (last path))
         refs (remove #(= % last) (::spec/ref-list side-content))]
     [:div.tile.is-vertical.is-parent
      [:div#side-content
       (if first-item [:div.is-hidden-mobile first-item])
       [:p (str refs)]]])))

(defn tile
  "renders a tile"
  [tile id size]
  (let [type-class (str "notification tile is-child " (get style-map (get tile :m-venue.spec/style)))
        href (::spec/href tile)
        id (str "tile-" id)]
    (if href
      [:a {:class type-class :href href :id id}
       [:p.title (::spec/nl-label (::spec/title tile))]
       (if-let [sub-title (::spec/nl-label (::spec/sub-title tile))]
         [:p.subtitle sub-title])
       (if-let [img-reference-data (repo/get-map :i (::spec/img tile))]
         (responsive-image (second img-reference-data) size))
       [:p (get-in tile [::spec/text ::spec/nl-text])]]
      [:div {:class type-class :id id}
       [:p.title (::spec/nl-label (::spec/title tile))]
       (if-let [sub-title (::spec/nl-label (::spec/sub-title tile))]
         [:p.subtitle sub-title])
       (if-let [img-reference-data (repo/get-map :i (::spec/img tile))]
         (responsive-image (second img-reference-data) size))
       [:p (get-in tile [::spec/text :m-venue.spec/nl-text])]])))

(defn main
  "renders content based on a general document"
  [main side reverse]
  [:section#main.section
   [:div.container
    (if reverse
      [:div.tile.is-ancestor.is-reversed
       main side]
      [:div.tile.is-ancestor
       main side])]])

(defn gd-content
  "renders content based on a general document"
  [id gd-map]
  [:div#main-content.tile.is-9.is-vertical {:data-document id}
   [:div.tile.is-parent
    (tile (::spec/tile gd-map) (str "gd-" 1) :l)]
   (let [all-tiles (::spec/tiles gd-map)
         split-tiles (split-at (/ (count all-tiles) 2) all-tiles)]
     [:div.tile.is-horizontal
      [:div#child-tiles-left.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 %1)) :m) (first split-tiles))]
      [:div#child-tiles-right.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 (count (first split-tiles)) %1)) :m) (second split-tiles))]])])

(defn first-comp
  [i1 i2]
  (compare (first i1) (first i2)))

(defn height-splitter
  [result value]
  (print value)
  (let [rel-height (get relative-height-map (::spec/img-css-class value))
        new-first [(+ rel-height (first (first result))) (conj (second (first result)) value)]]
    (sort first-comp (conj (rest result) new-first))))

(defn img-content
  "renders content based on a image document"
  [id image-map]
  [:div#main-content.tile.is-9.is-vertical {:data-document id}
   [:div.tile.is-parent
    (tile (::spec/tile image-map) "image-tile" :l)]
   (let [all-images (mapv #(second (repo/get-map :i %)) (::spec/image-list image-map))
         split-images (reverse (reduce height-splitter [[0 []] [0 []] [0 []]] all-images))]
     [:div.tile.is-horizontal.is-flex
      (for [[_ image-list] split-images] [:div.tile.is-4.is-vertical.is-parent
                                          (for [image-n image-list] [:div.tile.is-child.enlargeable-image {:id         (str "img-tile-" (::spec/base-path image-n))
                                                                                                           :data-src   (str (::spec/base-path image-n) "o.jpg")
                                                                                                           :data-title (get-in image-n [::spec/title :m-venue.spec/nl-label])
                                                                                                           :data-alt   (get-in image-n [::spec/alt :m-venue.spec/nl-label])
                                                                                                           :data-x     (get image-n :m-venue.spec/x-size)
                                                                                                           :data-y     (get image-n ::spec/y-size)}
                                                                     (responsive-image image-n :m)])])])])

(defn image-modal-style
  [src alt title scale]
  (let [m {:src   src
           :alt   alt
           :title title}]
    (if (and (number? scale) (< scale 100))
      (assoc m :style (str "max-width: " scale "%; margin-left: " (str (/ (- 100 scale) 2) "%")))
      m)))

(defn image-modal
  [src alt title scale]
  [:div#image-modal.modal.is-active
   [:div.modal-background]
   [:p.image
    [:img (image-modal-style src alt title scale)]]
   [:button.modal-close.is-large {:aria-label "close"}]])