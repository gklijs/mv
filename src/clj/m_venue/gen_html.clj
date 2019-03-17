(ns m-venue.gen-html
  (:require [m-venue.repo :as repo]
            [m-venue.spec :as spec]
            [m-venue.page-templates :as page-templates]))

(def redirects "resources/public/_redirects")

(defn write-html
  [_ key value]
  (if
    (not= :summary key)
    (doseq [language (keys (get-in (second value) [::spec/tile ::spec/texts]))]
      (let [page-name (if (= key :home) "index" (name key))
            context {::spec/language language ::spec/username "guest"}
            html (page-templates/content-page page-name value context false)
            file-name (str "resources/public/" (name language) "/" page-name ".html")]
        (if (= language :nl)
          (if
            (= key :home)
            (spit redirects "/ /nl 200\n" :append true)
            (spit redirects (str "/" page-name " /nl/" page-name " 200\n") :append true)))
        (clojure.java.io/make-parents file-name)
        (spit file-name html))
      )))

(defn delete-files-recursively [fname]
  (letfn [(delete-f [file]
            (when (.isDirectory file)
              (doseq [child-file (.listFiles file)]
                (delete-f child-file)))
            (clojure.java.io/delete-file file true))]
    (delete-f (clojure.java.io/file fname))))

(defn -main
  [& _]
  (delete-files-recursively "resources/public/img_dev")
  (delete-files-recursively "resources/public/img_test")
  (delete-files-recursively redirects)
  (repo/all-for-conn write-html :p)
  (spit redirects "/nl/* /nl/mispoes 404\n" :append true)
  (spit redirects "/en/* /en/mispoes 404\n" :append true)
  (spit redirects "/de/* /de/mispoes 404\n" :append true)
  (spit redirects "/* /nl/mispoes 404\n" :append true))