(defproject m-venue "0.1.0-SNAPSHOT"
  :description "Site for cat lovers"
  :url "https://github.com/nginx-clojure/nginx-clojure/tree/master/example-projects/clojure-web-example"
  :min-lein-version "2.0.0"
  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [com.taoensso/nippy "2.13.0"]
                 [compojure "1.6.0" :exclusions [ring/ring-core]]
                 [hiccups "0.3.0" :scope "provided"]
                 [image-resizer "0.1.10"]
                 [nl.klijs/spec-serialize "0.1.0-SNAPSHOT"]
                 [nginx-clojure "0.4.5" :scope "provided"]
                 [org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.908" :scope "provided"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [org.clojure/core.async "0.3.443" :scope "provided"]
                 [org.clojure/tools.reader "1.0.6"]]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "env/test/clj"]
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path "resources/public/js" "resources/public/css"]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-cooper "1.2.2"]
            [lein-sass "0.4.0" :exclusions [org.apache.commons/commons-compress org.clojure/clojure org.codehaus.plexus/plexus-utils]]
            [org.clojure/clojurescript "1.9.908"]]
  :profiles {
             :dev      {:dependencies [[javax.servlet/servlet-api "2.5"]
                                       [ring-mock "0.1.5"]]}
             :embed    {:dependencies
                                      [[nginx-clojure/nginx-clojure-embed "0.4.5"]
                                       [ring/ring-devel "1.6.2"]]
                        :aot          [m-venue.embed-server]
                        :main         m-venue.embed-server
                        :uberjar-name "m-venue-embed.jar"
                        :cljsbuild    {:builds {:app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                                                      :compiler
                                                                    {:main          "m-venue.app"
                                                                     :asset-path    "/js/out"
                                                                     :output-to     "resources/public/js/app.js"
                                                                     :output-dir    "resources/public/js/out"
                                                                     :source-map    true
                                                                     :optimizations :none
                                                                     :pretty-print  true}}
                                                :edit {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                                                      :compiler
                                                                    {:main          "m-venue.edit-app"
                                                                     :asset-path    "/js/edit-out"
                                                                     :output-to     "resources/public/js/edit.js"
                                                                     :output-dir    "resources/public/js/edit-out"
                                                                     :source-map    true
                                                                     :optimizations :none
                                                                     :pretty-print  true}}}}
                        :sass         {:src              "resources/app/stylesheets"
                                       :output-directory "resources/public/css"
                                       :source-maps      true
                                       :style            :nested}
                        :source-paths ["env/dev/clj"]
                        }
             :uberjar  {:omit-source    true
                        :cljsbuild      {:builds {:app
                                                  {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                                                   :compiler
                                                                 {:main          "m-venue.app"
                                                                  :output-to     "resources/public/js/app.js"
                                                                  :optimizations :advanced
                                                                  :pretty-print  false}}
                                                  :edit
                                                  {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                                                   :compiler
                                                                 {:main          "m-venue.edit-app"
                                                                  :output-to     "resources/public/js/edit.js"
                                                                  :optimizations :advanced
                                                                  :pretty-print  false}}}}
                        :sass           {:src              "resources/app/stylesheets"
                                         :output-directory "resources/public/css"
                                         :source-maps      false
                                         :style            :compressed}
                        :prep-tasks     ["compile" ["cljsbuild" "once" "app"] ["cljsbuild" "once" "edit"] ["sass" "once"]]
                        :aot            [m-venue.handler]
                        :uberjar-name   "m-venue-default.jar"
                        :source-paths   ["env/prod/clj"]
                        :resource-paths ["env/prod/resources"]
                        }
             })