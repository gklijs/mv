(ns m-venue.gen-util)

(defn get-by-language
  [map context]
  (if (nil? context)
    (second (first map))
    (if-let [v ((:m-venue.spec/language context) map)]
      v
      (second (first map)))))
