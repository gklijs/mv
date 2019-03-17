(ns m-venue.templates
  (:require [m-venue.constants :refer [image-sizes style-map relative-height-map flags-map]]
            [m-venue.gen-util :as gen-util]
            [m-venue.repo :as repo]
            [m-venue.spec :as spec]))

(defn add-img-srcset-part
  [img-reference srcset key size]
  (if
    (< size (::spec/x-size img-reference))
    (str srcset (::spec/base-path img-reference) (name key) ".jpg " size "w, ")
    (reduced srcset)))

(defn img-srcset
  [img-reference]
  (let
    [srcset (reduce-kv (partial add-img-srcset-part img-reference) "" image-sizes)]
    (str srcset (::spec/base-path img-reference) "o.jpg " (::spec/x-size img-reference) "w")))

(defn add-img-sizes-part
  [img-reference sizes _ size]
  (if
    (< size (::spec/x-size img-reference))
    (str sizes "(max-width: " size "px) " size "px, ")
    (reduced sizes)))

(defn img-sizes
  [img-reference]
  (let
    [sizes (reduce-kv (partial add-img-sizes-part img-reference) "" image-sizes)]
    (str sizes (::spec/x-size img-reference) "px")))

(defn small-square-img
  [[id img-summary]]
  (let [path (str (::spec/base-path img-summary) "64.jpg")]
    [:img {:id (str "img-select-" (name id)) :src path :data-id (str "i-" (name id))}]))

(defn all-images
  [all-images]
  [:figure#all-images (map small-square-img (::spec/img-summaries all-images))])

(defn responsive-image
  ([img-reference] (responsive-image img-reference nil))
  ([img-reference context] (responsive-image img-reference context nil))
  ([img-reference context extra-class]
   (let [ec (if (keyword? extra-class) (str " " (name extra-class)))
         image-meta (gen-util/get-by-language (::spec/img-meta-infos img-reference) context)]
     [:figure {:class (str "image " (::spec/img-css-class img-reference) ec)}
      [:img {:srcset (img-srcset img-reference)
             :sizes  (img-sizes img-reference)
             :src    (str "data:image/jpeg;base64," (::spec/base-64 img-reference))
             :title  (::spec/title image-meta)
             :alt    (::spec/alt image-meta)}]])))

(defn navbar-item
  [nav-item path parent-path]
  (let [has-children (and (nil? parent-path) (pos? (count (::spec/nav-children nav-item))))
        matching-part (if (nil? parent-path) (first path) (second path))
        is-active (if (and matching-part (= matching-part (::spec/p-reference nav-item))) " is-active")
        title [:span (::spec/n-title nav-item)]
        icon (if-let [icon-class (::spec/mdi-reference nav-item)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])
        [href target] (if-let [url (::spec/href nav-item)]
                        [url "_blank"]
                        [(str "/" (::spec/p-reference nav-item)) "_self"])]
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
  [path side-menu context other-languages]
  (let [id (if (string? path) path (last path))]
    [:nav#nav-bar.navbar.is-fixed-top {:role "navigation" :aria-label "main navigation"}
     [:div.navbar-brand
      (if side-menu
        [:button#burger-side-content.button.navbar-burger.is-hidden-tablet
         [:span]
         [:span]
         [:span]])
      [:a.navbar-item.is-tab
       {:href "/" :class (if (or (= "home" path) (= "index" path)) "is-active" "")}
       [:span.is-large "Martha's Venue"]]
      [:a.navbar-item.is-hidden-desktop
       {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
       [:span.icon {:style "color: #4267b2;"} [:i.mdi.mdi-24px.mdi-facebook]]]
      (for [language other-languages]
        [:a.navbar-item.is-hidden-desktop
         {:href (str "/" (name language) "/" id)} (language flags-map)])
      [:button#burger-menu.button.navbar-burger
       [:span]
       [:span]
       [:span]]]
     [:div#main-menu.navbar-menu
      (flex-main-menu path (second (repo/get-map :n (str "main-" (name (::spec/language context))))))
      [:div.navbar-end
       [:a.navbar-item.is-hidden-touch
        {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
        [:span.icon {:style "color: #4267b2;"} [:i.mdi.mdi-24px.mdi-facebook]]]
       (for [language other-languages]
         [:a.navbar-item.is-hidden-touch
          {:href (str "/" (name language) "/" id)} (language flags-map)])]]
     (if side-menu
       [:div#side-content.is-hidden side-menu])]))

(defn path-finder
  [result menu]
  (if (string? result)
    (if (= result (::spec/p-reference menu))
      (list result)
      (let [content-key-or-path (reduce path-finder result (::spec/nav-children menu))]
        (if (string? content-key-or-path)
          result
          (conj content-key-or-path (::spec/p-reference menu)))))
    result))

(defn get-path
  [content-key menu]
  (reduce path-finder content-key (::spec/nav-children menu)))

(defn side-menu-item
  [nav-item remaining-path]
  (let [p-reference (::spec/p-reference nav-item)
        is-active (if (and p-reference (= p-reference (first remaining-path))) " is-active")
        icon (if-let [icon-class (::spec/mdi-reference nav-item)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])
        [href target] (if p-reference
                        [(str "/" p-reference) "_self"]
                        [(::spec/href nav-item) "_blank"])]
    [:li [:a {:class is-active :href href :target target} icon (::spec/n-title nav-item)]
     (if-let [items (not-empty (::spec/nav-children nav-item))]
       [:ul.menu-list (map #(side-menu-item % (rest remaining-path)) items)])]))

(defn side-menu
  [level-two-child path]
  (let [icon (if-let [icon-class (::spec/mdi-reference level-two-child)] [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon-class)}]])]
    [:aside#side-menu.menu
     [:a.menu-label {:href (str "/" (second path))} icon (::spec/n-title level-two-child)]
     [:ul.menu-list (map #(side-menu-item % (drop 2 path)) (::spec/nav-children level-two-child))]]))

(defn side-menu?
  [path nav-item]
  (if-let [level-one-items (::spec/nav-children nav-item)]
    (if-let [level-one-child (first (filter #(= (first path) (::spec/p-reference %)) level-one-items))]
      (let [level-two-items (::spec/nav-children level-one-child)
            level-two-part (second path)]
        (if-let [level-two-child (first (filter #(= level-two-part (::spec/p-reference %)) level-two-items))]
          (if (pos? (count (::spec/nav-children level-two-child)))
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

(defn tile
  "renders a tile"
  ([t id context] (tile t id context nil))
  ([t id context url]
   (let [type-class (str "notification tile is-child " ((::spec/style t) style-map))
         href (or url (::spec/href t))
         target (if url "_self" "_blank")
         id (str "tile-" id)
         content (gen-util/get-by-language (::spec/texts t) context)]
     (if href
       [:a {:class type-class :href href :id id :target target}
        [:p.title (::spec/title content)]
        (if-let [sub-title (::spec/sub-title content)]
          [:p.subtitle sub-title])
        (if-let [img-reference-data (repo/get-map :i (::spec/img t))]
          (responsive-image (second img-reference-data) context))
        [:p (::spec/text content)]]
       [:div {:class type-class :id id}
        [:p.title (::spec/title content)]
        (if-let [sub-title (::spec/sub-title content)]
          [:p.subtitle sub-title])
        (if-let [img-reference-data (repo/get-map :i (::spec/img t))]
          (responsive-image (second img-reference-data) context))
        [:p (::spec/text content)]]))))

(defn s-content
  ([context] (s-content context nil))
  ([context path] (s-content context path nil))
  ([context path first-item]
   (let [language (name (::spec/language context))
         side-content (second (repo/get-map :n (str "side-" language)))
         last (if (list? path) (last path))
         refs (remove #(= % last) (::spec/ref-list side-content))]
     [:div.tile.is-vertical.is-parent
      (if first-item [:div#side-content.tile.is-child [:div.is-hidden-mobile first-item]])
      (for [ref refs]
        (if-let [content (repo/get-map :p ref)]
          (tile (::spec/tile (second content)) ref context (str "/" language "/" ref))))])))

(defn breadcrumb
  [p-reference context last]
  (let [language (name (::spec/language context))
        t (second (repo/get-map :p p-reference))
        content (gen-util/get-by-language (get-in t [::spec/tile ::spec/texts]) context)
        title (if content (::spec/title content) p-reference)]
    (if content
      (if last
        [:li.is-active [:a {:href       "#"
                            :aria-label "page"} title]]
        [:li [:a {:href (str "/" language "/" p-reference)} title]])
      (if last
        [:li.is-active [:p title]]
        [:li [:p title]]))))

(defn breadcrumbs
  [elements context]
  [:nav.breadcrumb.is-hidden-touch {:aria-label "breadcrumbs"}
   [:ul
    [:li [:a {:href "/"} "Home"]]
    (for [el (drop-last elements)]
      (breadcrumb el context false))
    (breadcrumb (last elements) context true)]])

(defn main
  "renders content based on a general document"
  [path context main side reverse]
  [:section#main.section
   [:div.container
    (cond
      (or (= path "login") (= path "home") (= path "index")) nil
      (string? path) (breadcrumbs (list path) context)
      (list? path) (breadcrumbs path context))
    (if reverse
      [:div.tile.is-ancestor.is-reversed
       main side]
      [:div.tile.is-ancestor
       main side])]])

(defn gd-content
  "renders content based on a general document"
  [id gd-map context]
  [:div#main-content.tile.is-9.is-vertical {:data-document id}
   [:div.tile.is-parent
    (tile (::spec/tile gd-map) (str "gd-" 1) context)]
   (let [all-tiles (filter #(get-in % [::spec/texts (::spec/language context)]) (::spec/tiles gd-map))
         split-tiles (split-at (/ (count all-tiles) 2) all-tiles)]
     [:div.tile.is-horizontal
      [:div#child-tiles-left.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 %1)) context) (first split-tiles))]
      [:div#child-tiles-right.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 (count (first split-tiles)) %1)) context) (second split-tiles))]])])

(defn first-comp
  [i1 i2]
  (compare (first i1) (first i2)))

(defn height-splitter
  [result value]
  (let [rel-height (get relative-height-map (::spec/img-css-class value))
        new-first [(+ rel-height (ffirst result)) (conj (second (first result)) value)]]
    (sort first-comp (conj (rest result) new-first))))

(defn img-content
  "renders content based on a image document"
  [id image-map context]
  [:div#main-content.tile.is-9.is-vertical {:data-document id}
   [:div.tile.is-parent
    (tile (::spec/tile image-map) "image-tile" context)]
   (let [all-images (mapv #(second (repo/get-map :i %)) (::spec/image-list image-map))
         split-images (reverse (reduce height-splitter [[0 []] [0 []] [0 []]] all-images))]
     [:div.tile.is-horizontal.is-flex
      (for [[_ image-list] split-images] [:div.tile.is-4.is-vertical.is-parent
                                          (for [image-n image-list]
                                            (let [image-meta (gen-util/get-by-language (::spec/img-meta-infos image-n) context)]
                                              [:div.tile.is-child {:id         (str "img-tile-" (::spec/base-path image-n))
                                                                   :data-src   (str (::spec/base-path image-n) "o.jpg")
                                                                   :data-title (::spec/title image-meta)
                                                                   :data-alt   (::spec/alt image-meta)
                                                                   :data-x     (::spec/x-size image-n)
                                                                   :data-y     (::spec/y-size image-n)}
                                               (responsive-image image-n context :enlargeable-image)]))])])])

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