(ns m-venue.demo-data
    (:require [m-venue.repo :as repo]))

(def home-page {:m-venue.spec/tile  {:m-venue.spec/title     {:m-venue.spec/nl-label "Welkom bij Martha's venue"}
                                     :m-venue.spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                     :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange introductie tot Martha's Venue en mogelijke links naar nieuws en nestjes enzo."}
                                     :m-venue.spec/style     :1}
                :m-venue.spec/tiles [{:m-venue.spec/title     {:m-venue.spec/nl-label "Alles over het voer"}
                                      :m-venue.spec/sub-title {:m-venue.spec/nl-label "Subtitle"}
                                      :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over het voeren van katten."}
                                      :m-venue.spec/style     :0}
                                     {:m-venue.spec/title     {:m-venue.spec/nl-label "Hoe maak ik de tuin veilig?"}
                                      :m-venue.spec/sub-title {:m-venue.spec/nl-label "Lees verder voor tips"}
                                      :m-venue.spec/text      {:m-venue.spec/nl-text "Een mogelijk erg lange text over de mogelijkheden om de tuin af te sluiten met hekken en netten"}
                                      :m-venue.spec/img       "uil.jpg"
                                      :m-venue.spec/href      "https://www.facebook.com/martha.huijser"
                                      :m-venue.spec/style     :1}
                                     {:m-venue.spec/title     {:m-venue.spec/nl-label "van Morren bestrating"}
                                      :m-venue.spec/sub-title {:m-venue.spec/nl-label "Ondertitel en link"}
                                      :m-venue.spec/text      {:m-venue.spec/nl-text "Ze hebben zowel als dakterras als onze tuin mooi en vakkundig afgedekt. Klik hier voor hun facebook"}
                                      :m-venue.spec/img       "blaat.jpg"
                                      :m-venue.spec/style     :2
                                      :m-venue.spec/href      "https://www.facebook.com/morrenbestrating"}
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
(def info-page {:m-venue.spec/tile  {:m-venue.spec/title     {:m-venue.spec/nl-label "Info over Martha's venue"}
                                     :m-venue.spec/sub-title {:m-venue.spec/nl-label "Door Martha"}
                                     :m-venue.spec/text      {:m-venue.spec/nl-text "Alle mogelijke manieren om in contact te komen met Martha's venue"}
                                     :m-venue.spec/style     :1}
                :m-venue.spec/tiles [{:m-venue.spec/title     {:m-venue.spec/nl-label "Alles over ons mooie huis"}
                                      :m-venue.spec/sub-title {:m-venue.spec/nl-label "Pieter de Koninghof 81"}
                                      :m-venue.spec/text      {:m-venue.spec/nl-text "Matha's venue is gelokalisserd op Pieter de koninghof 81, 3356 EH te papendrecht"}
                                      :m-venue.spec/style     :0}
                                     {:m-venue.spec/title {:m-venue.spec/nl-label "We zijn ook te vinden op facebook"}
                                      :m-venue.spec/text  {:m-venue.spec/nl-text "Regelmatig posen we foto's van de gekke fratsen die onze katten uithalen."}
                                      :m-venue.spec/href  "https://www.facebook.com/Marthasvenue"
                                      :m-venue.spec/style :5}
                                     ]})

(def initial-img-info {:m-venue.spec/img-path "resources/public/img/"
                       :m-venue.spec/latest-img 7})

(repo/set-map "p-home" :m-venue.spec/gen-doc home-page)
(repo/set-map "p-info" :m-venue.spec/gen-doc info-page)
(repo/set-map "i-info" :m-venue.spec/img-info initial-img-info)