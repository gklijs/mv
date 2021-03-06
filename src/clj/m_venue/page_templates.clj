(ns m-venue.page-templates
  (:require [hiccup.page :refer [html5]]
            [m-venue.editor-templates :refer :all]
            [m-venue.spec :as spec]
            [m-venue.templates :refer :all]
            [m-venue.repo :as repo]))

(def base-url "https://mv.gklijs.tech")

(defn page
  [id title app-bar content context editable]
  (html5 {:class "has-navbar-fixed-top" :lang (name (::spec/language context))}
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
         (if (or (= id "home") (= id "index"))
           [:link {:href (str base-url "/" (name (::spec/language context)) "/"), :rel "canonical"}]
           [:link {:href (str base-url "/" (name (::spec/language context)) "/" id), :rel "canonical"}])
         [:meta {:content "#ffffff", :name "msapplication-TileColor"}]
         [:meta {:content "/ms-icon-144x144.png", :name "msapplication-TileImage"}]
         [:meta {:content "#ffffff", :name "theme-color"}]
         [:link {:rel "stylesheet" :href "//cdn.materialdesignicons.com/3.0.39/css/materialdesignicons.min.css"}]
         [:link {:rel "stylesheet" :href "/css/mv.css"}]
         (if editable
           (edit-bars))
         app-bar
         content
         (footer)
         (if (true? editable)
           `([:script {:src "https://cdn.ckeditor.com/ckeditor5/12.0.0/balloon/ckeditor.js"}]
              [:script {:src "/js/edit.js"}])
           [:script {:src "/js/app.js"}])
         ))

(defn content
  [id content-key content-map context]
  (cond
    (= ::spec/gen-doc content-key) (gd-content id content-map context)
    (= ::spec/img-doc content-key) (img-content id content-map context)
    ))

(defn content-page
  [id [content-key content-map] context editable]
  (let [menu (second (repo/get-map :n (str "main-" (name (::spec/language context)))))
        path (get-path id menu)
        side-menu (side-menu? path menu)
        cont (content id content-key content-map context)
        other-languages (keys (dissoc (get-in content-map [::spec/tile ::spec/texts]) (::spec/language context)))]
    (page
      id
      (get-in content-map [::spec/tile :m-venue.spec/title ::spec/nl-label])
      (nav-bar path side-menu context other-languages)
      (if (nil? side-menu)
        (main path context cont (s-content context path) false)
        (main path context cont (s-content context path side-menu) true))
      context
      editable)))

(defn login-page
  [login-structure]
  (let [context {::spec/username "guest" ::spec/language :nl}]
    (page
      "login"
      "login"
      (nav-bar ["login"] nil context [])
      (main "login" context login-structure (s-content context) false)
      context
      false)))
