(ns m-venue.demo-data
  (:require [m-venue.repo :as repo]))

(def home-page {:m-venue.spec/title {:m-venue.spec/nl-label "Welkom bij Martha's Venue"}
                      :m-venue.spec/tiles [{:m-venue.spec/title {:m-venue.spec/nl-label "Alles over het voer"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                            :m-venue.spec/style :0}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Hoe maak ik de tuin veilig?"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Een mogelijk erg lange text over de mogelijkheden om de tuin af te sluiten met hekken en netten"}
                                            :m-venue.spec/img   "uil.jpg"
                                            :m-venue.spec/style :1}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "van Morren bestrating"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Ze hebben zowel als dakterras als onze tuin mooi en vakkundig afgedekt. Klik hier voor hun facebook"}
                                            :m-venue.spec/img   "blaat.jpg"
                                            :m-venue.spec/style :2
                                            :m-venue.spec/href "https://www.facebook.com/morrenbestrating"}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Van alles is mogelijk"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Dus ook dingen die nergens op slaan"}
                                            :m-venue.spec/style :3}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Dit wordt natuurlijk nog mooier"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Als Gerard tenminste genoeg tijd heeft"}
                                            :m-venue.spec/img   "uil.jpg"
                                            :m-venue.spec/style :4}
                                           {:m-venue.spec/title {:m-venue.spec/nl-label "Afbeeldingen worden ooit ook mogelijk"}
                                            :m-venue.spec/text  {:m-venue.spec/nl-text "Het systeem maakt dan zelf alle nodige varianten aan"}
                                            :m-venue.spec/img   "blaat.jpg"
                                            :m-venue.spec/style :5}
                                           ]})
(repo/set-map "mv-gd-home" home-page)
