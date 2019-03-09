(ns m-venue.gen-html
  (:require [m-venue.repo :as repo]
            [m-venue.page-templates :as page-templates]))

(defn write-html
  [_ key value]
  (if
    (not= :summary key)
    (let [html (page-templates/content-page (name key) value false)
          file-name (str "resources/public/nl/" (name key) ".html")]
      (clojure.java.io/make-parents file-name)
      (spit file-name html))))

(defn -main
  [& args]
  (repo/all-for-conn write-html :p))