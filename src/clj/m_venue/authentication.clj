(ns m-venue.authentication
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET POST]]
            [m-venue.admin-spec :as admin-spec]
            [m-venue.page-templates :as page-templates]
            [m-venue.users-db :as users-db]
            [ring.util.anti-forgery :refer [anti-forgery-field]])
  (:import (sun.security.util Password)))

(defonce guest-counter (atom 0))

(defn get-user [req]
  (if-let [logged-in-user (-> req :session :uid)]
    [logged-in-user false]
    (let [gc (swap! guest-counter inc)]
      [(str "guest-" gc) true])))

(defn is-editor
  [uid]
  (if-let [profile (users-db/get-profile uid)]
    (admin-spec/is-editor profile)
    false))

(defn handle-login [uid pass session]
  (log/debug "login with " uid ", old session :" session)
  (if-let [profile (users-db/get-profile uid)]
    (if (= pass (::admin-spec/password profile))
      {:status 303 :session (assoc session :uid uid) :headers {"Location" "/"}}
      {:status 303 :headers {"Location" "/login"}})
    {:status 303 :headers {"Location" "/login"}}))

(defroutes auth-routes
           (POST "/login" [uid pass :as {session :session}]
             (handle-login uid pass session))
           (GET "/login" []
             (page-templates/login-page
               [:div#main-content.tile.is-9.is-vertical
                [:div.tile.is-parent
                 [:div.content.notification.tile.is-child.is-primary
                  [:p.title "Log in om content aan te passen"]
                  [:div.input-group.panel-body
                   [:form.form-signin {:action "/login" :method "POST"}
                    (anti-forgery-field)
                    [:div.field [:p.control.has-icons-left
                                 [:input.input {:placeholder "User ID" :type :text :name :uid}]
                                 [:span.icon.is-small.is-left [:i.mdi.mdi-24px.mdi-account-circle]]]]
                    [:div.field [:p.control.has-icons-left
                                 [:input.input {:placeholder "Password" :type :password :name :pass}]
                                 [:span.icon.is-small.is-left [:i.mdi.mdi-24px.mdi-lock]]]]
                    [:div.field [:p.control [:button.button {:type "submit"} "Login!"]]]
                    ]]]]])))