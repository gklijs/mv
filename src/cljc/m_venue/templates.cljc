(ns m-venue.templates
  (:require [m-venue.constants :refer [image-sizes style-map]]
            [m-venue.repo :as repo]))

(defn get-correct-image
  [size x-size]
  (if (> x-size (get image-sizes size))
    size
    "o"))

(defn small-square-img
  [id]
  [:img {:id (str "img-select-" id) :src (str "/img/" id "/64.jpg") :data-id (str "i-" id)}])

(defn all-images
  [latest]
  [:figure#all-images (map small-square-img (take latest (iterate dec latest)))])

(defn responsive-image
  [img-reference size]
  [:figure {:class (str "image " (:m-venue.spec/img-css-class img-reference))}
   [:img {:src   (str (:m-venue.spec/base-path img-reference) (get-correct-image size (:m-venue.spec/x-size img-reference)) ".jpg")
          :title (get-in img-reference [:m-venue.spec/title :m-venue.spec/nl-label])
          :alt   (get-in img-reference [:m-venue.spec/alt :m-venue.spec/nl-label])}]])

(defn nav-bar
  [path]
  [:nav#nav-bar.navbar
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item.is-tab
      {:href "/" :class (if (or (= "/" path) (= "/home" path)) "is-active" "")}
      [:span.is-large "Martha's Venue"]]
     [:a.navbar-item.is-hidden-desktop
      {:target "_blank", :href "https://github.com/jgthms/bulma"}
      [:span.icon {:style "color: #333;"} [:i.mdi.mdi-24px.mdi-github-circle]]]
     [:a.navbar-item.is-hidden-desktop
      {:target "_blank", :href "https://twitter.com/jgthms"}
      [:span.icon {:style "color: #55acee;"} [:i.mdi.mdi-24px.mdi-twitter]]]
     [:div.navbar-burger.burger
      {:data-target "navMenubd-example"}
      [:span]
      [:span]
      [:span]]]
    [:div#navMenubd-example.navbar-menu
     [:div.navbar-start
      [:div.navbar-item.has-dropdown.is-hoverable
       [:a.navbar-link
        {:href "/documentation/overview/start/"}
        "\n          Docs\n        "]
       [:div.navbar-dropdown
        [:a.navbar-item
         {:href "/documentation/overview/start/"}
         "\n            Overview\n          "]
        [:a.navbar-item
         {:href "http://bulma.io/documentation/modifiers/syntax/"}
         "\n            Modifiers\n          "]
        [:a.navbar-item
         {:href "http://bulma.io/documentation/columns/basics/"}
         "\n            Columns\n          "]
        [:a.navbar-item
         {:href "http://bulma.io/documentation/layout/container/"}
         "\n            Layout\n          "]
        [:a.navbar-item
         {:href "http://bulma.io/documentation/form/general/"}
         "\n            Form\n          "]
        [:a.navbar-item
         {:href "http://bulma.io/documentation/elements/box/"}
         "\n            Elements\n          "]
        [:a.navbar-item.is-active
         {:href "http://bulma.io/documentation/components/breadcrumb/"}
         "\n              Components\n            "]
        [:hr.navbar-divider]
        [:div.navbar-item
         [:div
          [:p.is-size-6-desktop [:strong.has-text-info "0.5.1"]]
          [:small
           [:a.bd-view-all-versions
            {:href "/versions"}
            "View all versions"]]]]]]
      [:a.navbar-item.is-tab
       {:href "http://bulma.io/expo/"}
       [:span.icon [:i.mdi.mdi-24px.mdi-cat]] [:span "Cats"]]
      [:a.navbar-item.is-tab
       {:href "/info" :class (if (= "/info" path) "is-active" "")}
       [:span.icon [:i.mdi.mdi-24px.mdi-information-outline]] [:span "Info"]]]
     [:div.navbar-end
      [:a.navbar-item
       {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
       [:span.icon {:style "color: #4267b2;"} [:i.mdi.mdi-24px.mdi-facebook]]]]]]])

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

(defn side-content
  "renders the tiles on the right side"
  []
  [:div#side-content.tile.is-vertical.is-parent
   [:div.content.notification.tile.is-child
    [:div.control.field.has-icons-left
     [:input#chat.input {:type :text :placeholder "type and press ENTER to chat"}]
     [:span.icon.is-small.is-left [:i.mdi.mdi-24px.mdi-message-outline]]]
    [:div.field [:span.input-group-btn [:button#sendbtn.button.is-primary {:type :button} "Send!"]]]
    [:p#board.tile.is-vertical]]
   [:a.content.notification.tile.is-child {:href "/login"}
    [:p.title "Login"]
    [:div.image.is-3by4
     [:img {:src "/img/11/s.jpg"}]]
    [:p.subtitle "Klik op de notificatie om naar de pagina te gaan"]
    [:div.image.is-128x128
     [:img {:src "/img/11/256.jpg"}]]
    [:div.image.is-64x64
     [:img {:src (str "/img/11/64.jpg")}]]]])

(defn tile
  "renders a tile"
  [tile id size]
  (let [type-class (str "notification tile is-child " (get style-map (get tile :m-venue.spec/style)))
        href (:m-venue.spec/href tile)
        id (str "tile-" id)]
    (if href
      [:a {:class type-class :href href :id id}
       [:p.title (:m-venue.spec/nl-label (:m-venue.spec/sub-tile tile))]
       (if-let [sub-title (:m-venue.spec/nl-label (:m-venue.spec/sub-title tile))]
         [:p.subtitle sub-title])
       (if-let [img-reference-data (repo/get-map (str "i-" (:m-venue.spec/img tile)))]
         (responsive-image (second img-reference-data) size))
       [:p (get-in tile [:m-venue.spec/text :m-venue.spec/nl-text])]]
      [:div {:class type-class :id id}
       [:p.title (:m-venue.spec/nl-label (:m-venue.spec/sub-tile tile))]
       (if-let [sub-title (:m-venue.spec/nl-label (:m-venue.spec/sub-title tile))]
         [:p.subtitle sub-title])
       (if-let [img-reference-data (repo/get-map (str "i-" (:m-venue.spec/img tile)))]
         (responsive-image (second img-reference-data) size))
       [:p (get-in tile [:m-venue.spec/text :m-venue.spec/nl-text])]])))

(defn main
  "renders content based on a general document"
  [left right]
  [:section#main.section
   [:div.container
    [:div.tile.is-ancestor
     left right]]])

(defn gd-content
  "renders content based on a general document"
  [id gd-map]
  [:div#main-content.tile.is-9.is-vertical {:data-document id}
   [:div.tile.is-parent
    (tile (:m-venue.spec/tile gd-map) (str "gd-" 1) "l")]
   (let [all-tiles (:m-venue.spec/tiles gd-map)
         split-tiles (split-at (/ (count all-tiles) 2) all-tiles)]
     [:div.tile.is-horizontal
      [:div#child-tiles-left.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 %1)) "m" ) (first split-tiles))]
      [:div#child-tiles-right.tile.is-vertical.is-parent (map-indexed #(tile %2 (str "gd-" (+ 2 (count (first split-tiles)) %1)) "m") (second split-tiles))]])])