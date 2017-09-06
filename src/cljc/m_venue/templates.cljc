(ns m-venue.templates)

(def style-map
  {:0 ""
   :1 "is-primary"
   :2 "is-info"
   :3 "is-success"
   :4 "is-warning"
   :5 "is-danger"})

(defn nav-bar
  [path]
  [:nav.navbar
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item.is-tab
      {:href "/" :class (if (= "/" path) "is-active" "")}
      [:span.is-large "Martha's Venue"]]
     [:a.navbar-item.is-hidden-desktop
      {:target "_blank", :href "https://github.com/jgthms/bulma"}
      [:span.icon {:style "color: #333;"} [:i.fa.fa-github]]]
     [:a.navbar-item.is-hidden-desktop
      {:target "_blank", :href "https://twitter.com/jgthms"}
      [:span.icon {:style "color: #55acee;"} [:i.fa.fa-twitter]]]
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
       [:span.icon [:i.fa.fa-paw]] [:span "Cats"]]
      [:a.navbar-item.is-tab
       {:href "/info" :class (if (= "/info" path) "is-active" "")}
       [:span.icon [:i.fa.fa-info]] [:span "Info"]]]
     [:div.navbar-end
      [:a.navbar-item.is-tab.is-hidden-desktop-only
       {:target "_blank", :href "https://www.facebook.com/Marthasvenue"}
       [:span.icon {:style "color: #4267b2;"} [:i.fa.fa-facebook]]]]]]])

(defn footer
  "renders a footer"
  []
  [:footer.footer
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
       [:i.fa.fa-github]]]]]])

(defn side-content
  "renders the tiles on the right side"
  []
  [:div.tile.is-vertical.is-parent
   [:div.content.notification.tile.is-child
    [:div.control.field [:input#chat.input {:type :text :placeholder "type and press ENTER to chat"}]]
    [:div.field [:span.input-group-btn [:button#sendbtn.button.is-primary {:type :button} "Send!"]]]
    [:p#board.tile.is-vertical]
    ]
   [:a.content.notification.tile.is-child {:href "/login"}
    [:p.title "Login"]
    [:div.image.is-3by4
     [:img {:src "/img/gen/cat_in_a_box-small.jpg"}]]
    [:p.subtitle "Klik op de notificatie om naar de pagina te gaan"]
    [:div.image.is-128x128
     [:img {:src "/img/gen/cat_in_a_box-b-square.jpg"}]]
    [:div.image.is-64x64
     [:img {:src "/img/gen/cat_in_a_box-s-square.jpg"}]]]
   ]
  )

(defn tile
  "renders a tile"
  [tile]
  (let [type-class (str "content notification tile is-child " (get style-map (get tile :m-venue.spec/style)))
        href (get tile :m-venue.spec/href)]
    (if href
      [:a {:class type-class :href href}
       [:p.title (get-in tile [:m-venue.spec/title :m-venue.spec/nl-label])]
       (if-let [sub-title (get-in tile [:m-venue.spec/sub-title :m-venue.spec/nl-label])]
         [:p.subtitle sub-title])
       [:div.image.is-3by4
        [:img {:src "/img/gen/cat_in_a_box-large.jpg"}]]
       [:p (get-in tile [:m-venue.spec/text :m-venue.spec/nl-text])]
       ]
      [:div {:class type-class}
       [:p.title (get-in tile [:m-venue.spec/title :m-venue.spec/nl-label])]
       (if-let [sub-title (get-in tile [:m-venue.spec/sub-title :m-venue.spec/nl-label])]
         [:p.subtitle sub-title])
       [:div.image.is-3by4
        [:img {:src "/img/gen/cat_in_a_box-large.jpg"}]]
       [:p (get-in tile [:m-venue.spec/text :m-venue.spec/nl-text])]
       ])
    ))

(defn gd-content
  "renders content based on a general document"
  [gd-map]
  [:section.section
   [:div.container
    [:div.tile.is-ancestor
     [:div.tile.is-9.is-vertical
      [:div.tile.is-parent
       (tile (get gd-map :m-venue.spec/tile))]
      (let [all-tiles (get gd-map :m-venue.spec/tiles)
            split-tiles (split-at (/ (count all-tiles) 2) all-tiles)]
        [:div.tile.is-horizontal
         [:div.tile.is-vertical.is-parent (map #(tile %) (first split-tiles))
          [:div#app2.content.notification.tile.is-child]]
         [:div.tile.is-vertical.is-parent (map #(tile %) (second split-tiles))]])]
     (side-content)]]])
