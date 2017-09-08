(ns m-venue.chat
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [m-venue.websocket :refer [subscribe]]
            [nginx.clojure.core :as ncc]))

(def chatroom-users-channels (atom {}))
(def chatroom-topic)
(def sub-listener-removal-fn)

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
                  (ncc/send! ch (str "ch-" msg) true false)))))
  nil)

(subscribe
  "ch-"
  (fn [ch uid]
    (log/debug "user:" uid " connected!")
    (swap! chatroom-users-channels assoc uid ch)
    (ncc/pub! chatroom-topic (str uid ":[enter!]")))
  (fn [ch uid msg]
    (log/debug "user: " uid " msg " msg)
    (ncc/pub! chatroom-topic (str uid ":" msg)))
  (fn [ch uid reason]
    (log/debug "user:" uid " left! Because " reason)
    (swap! chatroom-users-channels dissoc uid)
    (ncc/pub! chatroom-topic (str uid ":[left!]")))
  )