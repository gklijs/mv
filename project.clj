(defproject m-vanue "0.1.0"
  :description "Site for cat lovers"
  :url "https://github.com/nginx-clojure/nginx-clojure/tree/master/example-projects/clojure-web-example"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [compojure "1.6.0" :exclusions [ring/ring-core]]
                 [jayq "2.5.4-resolve-fix" :scope "provided"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/clojurescript "1.9.908" :scope "provided"]
                 [org.clojure/core.async "0.3.443" :scope "provided"]
                 [org.clojure/tools.reader "1.0.5"]]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path "resources/public/js/app.js" "resources/public/js/out"]
  :plugins [[lein-cljsbuild "1.1.7"]
            [org.clojure/clojurescript "1.9.908"]]
  :profiles {
             :provided {:dependencies [[nginx-clojure "0.4.5"]]}
             :dev      {:dependencies [[javax.servlet/servlet-api "2.5"]
                                       [ring-mock "0.1.5"]]}
             :embed    {:dependencies
                                      [[nginx-clojure/nginx-clojure-embed "0.4.5"]
                                       [ring/ring-devel "1.6.2"]]
                        :aot          [m-venue.embed-server]
                        :main         m-venue.embed-server
                        :uberjar-name "m-venue-embed.jar"
                        :prep-tasks     ["compile" ["cljsbuild" "once" "app"]]
                        :cljsbuild    {:builds {:app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                                                 :compiler
                                                               {:main          "m-venue.app"
                                                                :asset-path    "/js/out"
                                                                :output-to     "resources/public/js/app.js"
                                                                :output-dir    "resources/public/js/out"
                                                                :source-map    true
                                                                :optimizations :none
                                                                :pretty-print  true}}}}
                        :source-paths ["env/dev/clj"]
                        }
             :uberjar  {:omit-source    true
                        :prep-tasks     ["compile" ["cljsbuild" "once" "min"]]
                        :cljsbuild      {:builds  {:min
                                                  {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                                                   :compiler
                                                                 {:output-to     "resources/public/js/app.js"
                                                                  :optimizations :advanced
                                                                  :pretty-print  false
                                                                  :closure-warnings
                                                                                 {:externs-validation :off :non-standard-jsdoc :off}
                                                                  :externs       ["env/prod/resources/jquery-externs-3.2.js"]}}}}
                        :aot            [m-venue.handler]
                        :uberjar-name   "m-venue-default.jar"
                        :source-paths   ["env/prod/clj"]
                        :resource-paths ["env/prod/resources"]
                        }
             })
