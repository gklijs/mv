(ns m-venue.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [m-venue.handler :refer :all]))


(deftest test-app
  (testing "main route"
    (let [response (app-routes (mock/request :get "/hello1"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World!"))))

  (testing "not-found route"
    (let [response (app-routes (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
