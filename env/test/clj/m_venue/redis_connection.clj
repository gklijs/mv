(ns m-venue.redis-connection)

(defonce i-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 0}})
(defonce p-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 1}})
(defonce n-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 2}})
(defonce u-conn {:pool {:max-total 4} :spec {:uri "redis://localhost:8081" :db 15}})