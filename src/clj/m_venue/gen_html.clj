(ns m-venue.gen-html
  (:require [m-venue.repo :as repo]
            [m-venue.page-templates :as page-templates]))

(defn write-html
  [_ key value]
  (if
    (not= :summary key)
    (let [html (page-templates/content-page (name key) value false)]
      (spit (str "resources/public/nl/" (name key) ".html") html))))

(defn -main
  [& args]
  (repo/all-for-conn write-html :p))