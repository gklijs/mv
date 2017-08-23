(ns m-venue.templates
  (:require [hiccup.page :as hiccup]))

(defn nav-bar
  []
  [:nav.navbar
   [:div.navbar-brand
    [:a.navbar-item
     {:href "http://marthasvenue.nl"}
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
     [:a.navbar-item
      {:href "http://bulma.io/expo/"}
      [:span.icon [:i.fa.fa-paw]][:span "Cats"]]
     [:a.navbar-item
      {:href "http://bulma.io/love/"}
      [:span.icon [:i.fa.fa-info]][:span "Info"]]]
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
     [:link {:rel "stylesheet" :href "https://jenil.github.io/bulmaswatch/simplex/bulmaswatch.min.css"}]
      app-bar
      content
     [:script {:src "https://code.jquery.com/jquery-3.2.1.min.js"}]
     [:script {:src "/js/app.js"}]))
