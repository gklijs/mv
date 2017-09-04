(ns m-venue.authentication
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET POST]]
            [m-venue.templates :as templates]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defonce guest-counter (atom 0))

(defn get-user [req]
  (if-let [logged-in-user (-> req :session :uid)]
    logged-in-user
    (do
      (swap! guest-counter inc)
      (str "guest-" @guest-counter))
    ))

(defn handle-login [uid pass session]
  "Here we can add server-side auth. In this example we'll just always authenticate
   the user successfully regardless what inputted."
  (log/debug "login with " uid ", old session :" session)
  ;; redirect to /
  {:status 303 :session (assoc session :uid uid) :headers {"Location" "/"}})

(defroutes auth-routes
           (POST "/login" [uid pass :as {session :session}]
             (handle-login uid pass session))
           (GET "/login" []
             (templates/page
               "login page"
               (templates/nav-bar :login)
               [:div.container
                [:div.panel.panel-primary
                 [:div.panel-heading [:h3.panel-title "Login Form"]]
                 [:div.input-group.panel-body
                  [:form.form-signin {:action "/login" :method "POST"}
                   [:h2.form-signin-heading "Please sign in"]
                   (anti-forgery-field)
                   [:input#user-id.form-control {:type :text :name :uid :placeholder "User ID"}]
                   [:input#user-pass.form-control {:type :password :name :pass :placeholder "Password"}]
                   [:p]
                   [:input#submit-btn.btn.btn-primary.btn-block {:type "submit" :value "Login!"}]
                   ]]
                 [:div.panel-footer]]])))
