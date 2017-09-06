(ns m-venue.websocket
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET]]
            [clojure.string :as string]
            [m-venue.authentication :refer [get-user]]
            [nginx.clojure.core :as ncc]
            [spec-serialize.impl :as tf]))

(def chatroom-users-channels (atom {}))
(def chatroom-topic)
(def sub-listener-removal-fn)

(def correct-gen-doc {:m-venue.spec/tile  {:m-venue.spec/title     {:m-venue.spec/nl-label "Alles over katten"}
                                           :m-venue.spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                           :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over katten."}
                                           :m-venue.spec/style     :1}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                            :m-venue.spec/style :4
                                            :m-venue.spec/href  "http://www.nu.nl"}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                            :m-venue.spec/img   "uil.jpg"
                                            :m-venue.spec/style :3}
                                           ]})

;; Because if we use embeded nginx-clojure the nginx-clojure JNI methods
;; won't be registered until the first startup of the nginx server so we need
;; use delayed initialization to make sure some initialization work
;; to be done after nginx-clojure JNI methods being registered.
(defn jvm-init-handler [_]
  ;; init chatroom topic
  ;; When worker_processes  > 1 in nginx.conf, there're more than one JVM instances
  ;; and requests from the same session perphaps will be handled by different JVM instances.
  ;; We need setup subscribing message listener here to get chatroom messages from other JVM instances.
  ;; The below graph show the message flow in a chatroom

  ;            \-----/     (1)send  (js)    +-------+
  ;            |User1| -------------------->|WorkerA|
  ;            /-----\                      +-------+
  ;               ^                           | |
  ;               |       (3)send!            | |(2)pub!
  ;               '---------------------------' |
  ;                                             V
  ;             \-----/   (3)send!          +-------+
  ;             |User2| <------------------ |WorkerB|
  ;             /-----\                     +-------+
  (def chatroom-topic (ncc/build-topic! "chatroom-topic"))
  ;; avoid duplicate adding when auto-reload namespace is enabled in dev enviroments.
  (when (bound? #'sub-listener-removal-fn) (sub-listener-removal-fn))
  (def sub-listener-removal-fn
    (ncc/sub! chatroom-topic nil
              (fn [msg _]
                (doseq [[uid ch] @chatroom-users-channels]
                  (ncc/send! ch (str "ch-" msg) true false)
                  (ncc/send! ch (str "mc-" (tf/to-string :m-venue.spec/gen-doc correct-gen-doc)) true false)))))
  nil)

(defroutes web-socket-route
           ;; Websocket server endpoint
           (GET "/ws" [:as req]
             (let [ch (ncc/hijack! req true)
                   uid (get-user req)]
               (when (ncc/websocket-upgrade! ch true)
                 (ncc/add-aggregated-listener! ch 500
                                               {:on-open    (fn [ch]
                                                              (log/debug "user:" uid " connected!")
                                                              (swap! chatroom-users-channels assoc uid ch)
                                                              (ncc/pub! chatroom-topic (str uid ":[enter!]")))
                                                :on-message (fn [ch msg]
                                                              (log/debug "user:" uid " msg:" msg)
                                                              (cond
                                                                (string/starts-with? msg "ch-") (ncc/pub! chatroom-topic (str uid ":" (subs msg 3)))
                                                                :else (log/error "Unknown message type: " msg))
                                                              )
                                                :on-close   (fn [ch reason]
                                                              (log/debug "user:" uid " left!")
                                                              (swap! chatroom-users-channels dissoc uid)
                                                              (ncc/pub! chatroom-topic (str uid ":[left!]")))})
                 {:status 200 :body ch})))
           )
