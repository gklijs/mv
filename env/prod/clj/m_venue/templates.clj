(ns m-venue.templates
  (:require [hiccup.page :as hiccup]))

(defn render-page
  [title content]
  (hiccup/html5
    [:title title]
    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700|Material+Icons"}]
    [:link {:rel "stylesheet" :href "https://unpkg.com/vuetify/dist/vuetify.min.css"}]
    content
    [:script {:src "https://unpkg.com/vue/dist/vue.min.js"}]
    [:script {:src "https://unpkg.com/vuetify/dist/vuetify.min.js"}]
    [:script {:src "/js/app.js"}]))
