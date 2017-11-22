(ns m-venue.embed-server
  (:gen-class)
  (:use [m-venue.handler])
  (:require [nginx.clojure.embed :as embed]
            [clojure.tools.logging :as log]
            [m-venue.demo-data :as demo-data]
            [m-venue.chat :refer [jvm-init-handler]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import (java.awt HeadlessException Desktop)
           (java.net URI)))

(defn start-server
  "Run an emebed nginx-clojure for debug/test usage."
  [dev? port]
  (embed/run-server
    (if dev?
      ;; Use wrap-reload to enable auto-reload namespaces of modified files
      ;; DO NOT use wrap-reload in production enviroment
      (do
        (log/info "enable auto-reloading in dev enviroment")
        (wrap-reload #'app))
      app)
    {:port               port
     ;;setup jvm-init-handler
     :jvm-init-handler   jvm-init-handler
     ;; define shared map for PubSubTopic
     :http-user-defined, "shared_map PubSubTopic tinymap?space=1m&entries=256;\n
                          shared_map mySessionStore tinymap?space=1m&entries=256;"}))

(defn stop-server
  "Stop the embed nginx-clojure"
  []
  (embed/stop-server))

(defn -main
  [& args]
  (let [dev? (empty? args)
        port (or (first args) 8080)
        port (start-server dev? port)]
    (when-not (System/getProperty "java.awt.headless")
      (try
        (.browse (Desktop/getDesktop) (URI. (str "http://localhost:" port "/")))
        (catch HeadlessException _)))))


