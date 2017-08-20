(ns m-venue.templates
  (:require [hiccup.page :as hiccup]))

(defn nav-bar
  []
  [:nav.navbar
   [:div.navbar-brand
    [:a.navbar-item
     {:href "http://bulma.io"}
     [:img
      {:height "28",
       :width "112",
       :alt "Bulma: a modern CSS framework based on Flexbox",
       :src "http://bulma.io/images/bulma-logo.png"}]]
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
      [:a.navbar-link.is-active
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
     [:div.navbar-item.has-dropdown.is-hoverable
      [:a.navbar-link
       {:href "http://bulma.io/blog/"}
       "\n          Blog\n        "]
      [:div#blogDropdown.navbar-dropdown
       {:data-style "width: 18rem;"}
       [:a.navbar-item {:href "/2017/08/03/list-of-tags/"}]
       [:div.navbar-content
        [:p [:small.has-text-info "03 Aug 2017"]]
        [:p "New feature: list of tags"]]
       [:a.navbar-item
        {:href "/2017/08/01/bulma-bootstrap-comparison/"}]
       [:div.navbar-content
        [:p [:small.has-text-info "01 Aug 2017"]]
        [:p "Bulma / Bootstrap comparison"]]
       [:a.navbar-item
        {:href "/2017/07/24/access-previous-bulma-versions/"}]
       [:div.navbar-content
        [:p [:small.has-text-info "24 Jul 2017"]]
        [:p "Access previous Bulma versions"]]
       [:a.navbar-item
        {:href "http://bulma.io/blog/"}
        "\n            More posts\n          "]
       [:hr.navbar-divider]
       [:div.navbar-item
        [:div.navbar-content
         [:div.level.is-mobile
          [:div.level-left
           [:div.level-item [:strong "Stay up to date!"]]]
          [:div.level-right
           [:div.level-item
            [:a.button.bd-is-rss.is-small
             {:href "http://bulma.io/atom.xml"}
             [:span.icon.is-small [:i.fa.fa-rss]]
             [:span "Subscribe"]]]]]]]]]
     [:div.navbar-item.has-dropdown.is-hoverable
      [:div.navbar-link "\n          More\n        "]
      [:div#moreDropdown.navbar-dropdown
       [:a.navbar-item {:href "http://bulma.io/extensions/"}]
       [:div.level.is-mobile
        [:div.level-left
         [:div.level-item
          [:p
           [:strong "Extensions"]
           [:br]
           [:small "Side projects to enhance Bulma"]]]]
        [:div.level-right
         [:div.level-item
          [:span.icon.has-text-info [:i.fa.fa-plug]]]]]]]
     [:a.navbar-item
      {:href "http://bulma.io/expo/"}
      [:span.bd-emoji "🎨"]
      "\n        Expo\n      "]
     [:a.navbar-item
      {:href "http://bulma.io/love/"}
      [:span.bd-emoji "❤️"]
      "\n        Love\n      "]]
    [:div.navbar-end
     [:a.navbar-item.is-hidden-desktop-only
      {:target "_blank", :href "https://github.com/jgthms/bulma"}
      [:span.icon {:style "color: #333;"} [:i.fa.fa-github]]]
     [:a.navbar-item.is-hidden-desktop-only
      {:target "_blank", :href "https://twitter.com/jgthms"}
      [:span.icon {:style "color: #55acee;"} [:i.fa.fa-twitter]]]
     [:div.navbar-item
      [:div.field.is-grouped
       [:p.control
        [:a.bd-tw-button.button
         {:href
                               "https://twitter.com/intent/tweet?text=Bulma: a modern CSS framework based on Flexbox&hashtags=bulmaio&url=http://bulma.io&via=jgthms",
          :target "_blank",
          :data-social-target "http://bulma.io",
          :data-social-action "tweet",
          :data-social-network "Twitter"}
         [:span.icon [:i.fa.fa-twitter]]
         [:span "\n    Tweet\n  "]]]
       [:p.control
        [:a.button.is-primary
         {:href "https://github.com/jgthms/bulma/archive/0.5.1.zip"}
         [:span.icon [:i.fa.fa-download]]
         [:span "Download"]]]]]]]])

(defn page
  [title app-bar content]
    (hiccup/html5
      [:meta {:charset "utf-8"}]
      [:meta
       {:content "width=device-width, initial-scale=1", :name "viewport"}]
     [:title title]
     [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"}]
     [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.5.1/css/bulma.min.css"}]
      app-bar
      content
     [:script {:src "https://code.jquery.com/jquery-3.2.1.min.js"}]
     [:script {:src "/js/chat.js"}]
     [:script {:src "/js/app.js"}]))
