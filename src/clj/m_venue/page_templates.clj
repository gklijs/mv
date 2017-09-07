(ns m-venue.page-templates
  (:require [hiccup.page :refer [html5]]
            [m-venue.templates :refer :all]))
(defn page
  [title app-bar content]
  (html5
    [:meta {:charset "utf-8"}]
    [:meta {:content "width=device-width, initial-scale=1", :name "viewport"}]
    [:title title]
    [:link {:href "/apple-icon-57x57.png", :sizes "57x57", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-60x60.png", :sizes "60x60", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-72x72.png", :sizes "72x72", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-76x76.png", :sizes "76x76", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-114x114.png", :sizes "114x114", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-120x120.png", :sizes "120x120", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-144x144.png", :sizes "144x144", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-152x152.png", :sizes "152x152", :rel "apple-touch-icon"}]
    [:link {:href "/apple-icon-180x180.png", :sizes "180x180", :rel "apple-touch-icon"}]
    [:link {:href "/android-icon-192x192.png", :sizes "192x192", :type "image/png", :rel "icon"}]
    [:link {:href "/favicon-32x32.png", :sizes "32x32", :type "image/png", :rel "icon"}]
    [:link {:href "/favicon-96x96.png", :sizes "96x96", :type "image/png", :rel "icon"}]
    [:link {:href "/favicon-16x16.png", :sizes "16x16", :type "image/png", :rel "icon"}]
    [:link {:href "/manifest.json", :rel "manifest"}]
    [:meta {:content "#ffffff", :name "msapplication-TileColor"}]
    [:meta {:content "/ms-icon-144x144.png", :name "msapplication-TileImage"}]
    [:meta {:content "#ffffff", :name "theme-color"}]
    [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"}]
    [:link {:rel "stylesheet" :href "/css/mv.css"}]
    app-bar
    content
    (footer)
    [:script {:src "/js/app.js"}]))

(defn gd-page
  [gd-map req]
  (page
    (get-in gd-map [:m-venue.spec/tile :m-venue.spec/title :m-venue.spec/nl-label])
    (nav-bar (:uri req))
    (main (gd-content gd-map) (side-content))))
