(ns m-venue.handler
  (:require [compojure.core :refer [defroutes GET routes]]
            [compojure.route :as route]
            [m-venue.authentication :refer [auth-routes get-user is-editor]]
            [m-venue.constants :refer [flags-map]]
            [m-venue.repo :as repo]
            [m-venue.repo-bridge]
            [m-venue.spec]
            [m-venue.page-templates :as page-templates]
            [m-venue.websocket :refer [web-socket-routes]]
            [nginx.clojure.session]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def my-session-store
  ;; When worker_processes  > 1 in nginx.conf, we can not use the default in-memory session store
  ;; because there're more than one JVM instances and requests from the same session perhaps
  ;; will be handled by different JVM instances. So here we use cookie store, or nginx shared map store
  ;; and if we use redis to shared sessions we can try [carmine-store] (https://github.com/ptaoussanis/carmine) or
  ;; [redis session store] (https://github.com/wuzhe/clj-redis-session)

  ;; use cookie store
  ;(ring.middleware.session.cookie/cookie-store {:key "a 16-byte secret"})

  ;; use nginx shared map store
  (nginx.clojure.session/shared-map-store "mySessionStore"))

(defn main-response
  [req lang page]
  (let [language (if (lang flags-map) lang (first (first flags-map)))
        uid (get-user req)
        context {:m-venue.spec/language language :m-venue.spec/username uid}]
    (if-let [content (repo/get-map :p page)]
      (page-templates/content-page page content context (is-editor uid))
      (route/not-found (page-templates/content-page "mispoes" (repo/get-map :p "mispoes") context (is-editor (get-user req)))))))

(defroutes app-routes
           ;; home page
           (GET "/" [:as req]
             (main-response req :nl "home"))
           (GET "/:page" [page :as req]
             (main-response req :nl page))
           (GET "/:lang/:page" [lang page :as req]
             (main-response req (keyword lang) page)))

(def app
  (wrap-defaults (routes auth-routes web-socket-routes app-routes)
                 (update-in site-defaults [:session]
                            assoc :store my-session-store)))
