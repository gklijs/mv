(ns m-venue.demo-data
  (:require [m-venue.repo :as repo]))

(def home-page {:m-venue.spec/title {:m-venue.spec/nl-label "Welkom bij Martha's Venue"}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Alles over speeltjes"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over speeltjes voor katten."}
                                            :m-venue.spec/img   "uil.jpg"}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "En nog wat meer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Hier kan dus van alles en nogwat staan"}
                                            :m-venue.spec/img   "blaat.jpg"}
                                           ]})

(defn init!
  []
  (repo/set-map "mv-gd-home" home-page))
