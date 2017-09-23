(ns m-venue.handler
  (:require [compojure.core :refer [defroutes GET routes]]
            [compojure.route :as route]
            [m-venue.authentication :refer [auth-routes get-user is-editor]]
            [m-venue.chat]
            [m-venue.demo-data :as demo-data]
            [m-venue.repo :as repo]
            [m-venue.repo-bridge]
            [m-venue.spec]
            [m-venue.page-templates :as page-templates]
            [m-venue.websocket :refer [web-socket-routes]]
            [nginx.clojure.core :as ncc]
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

(defroutes app-routes
           ;; home page
           (GET "/" [:as req]
             (if-let [home-gd (repo/get-map "p-home")]
               (let [[uid new] (get-user req)
                     body (page-templates/gd-page (second home-gd) req (is-editor uid))]
                 (if new
                   {:status  200
                    :headers {"Content-Type" "text/html; charset=utf-8"}
                    :body    body
                    :session (assoc (:session req) :uid uid)}
                   body))
               (route/not-found "Not Found")
               ))
           ;; Other general document pages
           (GET "/:id" [id :as req]
             (if-let [some-gd (repo/get-map (str "p-" id))]
               (let [[uid new] (get-user req)
                     body (page-templates/gd-page (second some-gd) req (is-editor uid))]
                 (if new
                   {:status  200
                    :headers {"Content-Type" "text/html; charset=utf-8"}
                    :body    body
                    :session (assoc (:session req) :uid uid)}
                   body))
               (route/not-found "Not Found")
               ))
           ;; Static files, e.g js/chat.js in dir `public`
           ;; In production environments it will be overwrited by
           ;; nginx static files service, see conf/nginx.conf
           (route/resources "/js")
           (route/resources "/css")
           (route/not-found "Not Found"))

(def app
  (wrap-defaults (routes auth-routes web-socket-routes app-routes)
                 (update-in site-defaults [:session]
                            assoc :store my-session-store)))
