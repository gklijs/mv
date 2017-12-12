(ns m-venue.redis-test
  (:require [clojure.test :refer :all]
            [m-venue.spec]
            [m-venue.admin-spec :as as]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.nippy :as nippy]
            [spec-serialize.core :as s-core]))

(def correct-profile {::as/username "Gerard Klijs"
                      ::as/email    "g.klijs@gmail.com"
                      ::as/password "test1234"
                      ::as/role     "admin"})

(def correct-profile2 {::as/username "Martha Huijser"
                       ::as/email    "teigetje77@gmail.com"
                       ::as/password "test1234"
                       ::as/role     "editor"})

(def serveri-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 0}}) ; See `wcar` docstring for opts
(def serverp-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 1}}) ; See `wcar` docstring for opts
(def servern-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 2}}) ; See `wcar` docstring for opts
(def serveru-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 15}}) ; See `wcar` docstring for opts
(defmacro wcari* [& body] `(car/wcar serveri-conn ~@body))
(defmacro wcarp* [& body] `(car/wcar serverp-conn ~@body))
(defmacro wcarn* [& body] `(car/wcar servern-conn ~@body))
(defmacro wcaru* [& body] `(car/wcar serveru-conn ~@body))

(defn store
  [type-s key data]
  (let [binary (nippy/freeze (read-string data))]
    (cond
      (= "i" type-s) (wcari* (car/set key binary))
      (= "p" type-s) (wcarp* (car/set key binary))
      (= "n" type-s) (wcarn* (car/set key binary))
      )))

(deftest redis-users
  (testing "users"
    (wcaru* (car/set "gklijs" [::as/profile (s-core/ser-map ::as/profile correct-profile)]))
    (wcaru* (car/set "mhuijser" [::as/profile (s-core/ser-map ::as/profile correct-profile2)]))
    (print (wcaru* (car/get "gklijs")))
    ))

(deftest redis-get
  (testing "get"
    (println (let [[spec value] (wcari* (car/get "1"))] (s-core/de-ser-vector spec value)))
    (println (let [[spec value] (wcarp* (car/get "home"))] (s-core/de-ser-vector spec value)))
    (println (let [[spec value] (wcarn* (car/get "main-nl"))] (s-core/de-ser-vector spec value)))
    (println (wcari* (car/get "home")))
    (println (wcarp* (car/get "main-nl")))
    (println (wcarn* (car/get "1")))
    ))


