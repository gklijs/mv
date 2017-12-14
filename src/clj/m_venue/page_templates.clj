(ns m-venue.page-templates
  (:require [hiccup.page :refer [html5]]
            [m-venue.editor-templates :refer :all]
            [m-venue.spec :as spec]
            [m-venue.templates :refer :all]
            [m-venue.repo :as repo]))
(defn page
  [title app-bar content editable]
  (html5 {:class "has-navbar-fixed-top" :lang "nl"}
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
         [:link {:rel "stylesheet" :href "//cdn.materialdesignicons.com/2.0.46/css/materialdesignicons.min.css"}]
         [:link {:rel "stylesheet" :href "/css/mv.css"}]
         (if editable
           (edit-bars))
         app-bar
         content
         (footer)
         (if (true? editable)
           `([:script {:src "https://cdn.ckeditor.com/ckeditor5/1.0.0-alpha.2/balloon/ckeditor.js"}]
              [:script {:src "/js/edit.js"}])
           [:script {:src "/js/app.js"}])
         ))

(defn content
  [id content-key content-map]
  (cond
    (= ::spec/gen-doc content-key) (gd-content id content-map)
    (= ::spec/img-doc content-key) (img-content id content-map)
    ))

(defn content-page
  [id [content-key content-map] path editable]
  (page
    (get-in content-map [:m-venue.spec/tile :m-venue.spec/title :m-venue.spec/nl-label])
    (nav-bar path)
    (if-let [side-menu-nl (side-menu? path (second (repo/get-map :n "main-nl")))]
      (main (side-content side-menu-nl) (content id content-key content-map))
      (main (content id content-key content-map) (side-content)))
    editable))

(defn login-page
  [login-structure]
  (page
    "login"
    (nav-bar ["login"])
    (main login-structure (side-content))
    false))
