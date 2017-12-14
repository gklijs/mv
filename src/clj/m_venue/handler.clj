(ns m-venue.handler
  (:require [compojure.core :refer [defroutes GET routes]]
            [compojure.route :as route]
            [m-venue.authentication :refer [auth-routes get-user is-editor]]
            [m-venue.chat]
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
  [req path]
  (let [main-doc (if path (last path) "home")]
    (if-let [content (repo/get-map :p main-doc)]
      (let [[uid new] (get-user req)
            body (page-templates/content-page main-doc content path (is-editor uid))]
        (if new
          {:status  200
           :headers {"Content-Type" "text/html; charset=utf-8"}
           :body    body
           :session (assoc (:session req) :uid uid)}
          body))
      (route/not-found "Not Found"))))

(defroutes app-routes
           ;; home page
           (GET "/" [:as req]
             (main-response req nil))
           (GET "/:p1" [p1 :as req]
             (main-response req [p1]))
           (GET "/:p1/:p2" [p1 p2 :as req]
             (main-response req [p1 p2]))
           (GET "/:p1/:p2/:p3" [p1 p2 p3 :as req]
             (main-response req [p1 p2 p3]))
           (GET "/:p1/:p2/:p3/:p4" [p1 p2 p3 p4 :as req]
             (main-response req [p1 p2 p3 p4]))
           (GET "/:p1/:p2/:p3/:p4/:p5" [p1 p2 p3 p4 p5 :as req]
             (main-response req [p1 p2 p3 p4 p5]))
           (GET "/:p1/:p2/:p3/:p4/:p5/:p6" [p1 p2 p3 p4 p5 p6 :as req]
             (main-response req [p1 p2 p3 p4 p5 p6]))
           (GET "/:p1/:p2/:p3/:p4/:p5/:p6/:p7" [p1 p2 p3 p4 p5 p6 p7 :as req]
             (main-response req [p1 p2 p3 p4 p5 p6 p7])))

(def app
  (wrap-defaults (routes auth-routes web-socket-routes app-routes)
                 (update-in site-defaults [:session]
                            assoc :store my-session-store)))
