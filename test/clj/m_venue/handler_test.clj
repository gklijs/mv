(ns m-venue.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [m-venue.handler :refer :all]))

(deftest test-app
  (testing "not-found route"
    (let [response (app-routes (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
