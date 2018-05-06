(ns m-venue.editor-templates
  (:require [m-venue.constants :refer [style-map]]))

(defn button
  ([id style icon] (button id style icon false))
  ([id style icon disabled]
   [:p.control [:button {:id id :class (str "button " (style style-map)) :disabled disabled}
                [:span.icon [:i {:class (str "mdi mdi-24px mdi-" icon)}]]]]))

(defn edit-buttons
  []
  [:div#edit-buttons.field.is-grouped.is-grouped-multiline
   [:input#upload-image-files {:name "upload-image-files" :type "file" :accept "image/jpeg" :multiple "" :style "display: none;"}]
   [:p.control [:button#upload-image-button.button.is-primary
                [:span.icon [:i.mdi.mdi-24px.mdi-upload]]]]
   [:p.control [:button#image-selection-button.button.is-black.is-outlined
                [:span.icon [:i.mdi.mdi-24px.mdi-camera]]]]
   [:p.control [:img#small-selected-image]]
   [:p.control [:button#edit-main-button.button.is-success.is-outlined
                [:span.icon [:i.mdi.mdi-24px.mdi-pen]]]]
   [:p.control [:button#clear-storage-button.button.is-danger
                [:span.icon [:i.mdi.mdi-24px.mdi-delete]]]]])

(defn image-selection-columns
  []
  [:div#image-selection-columns.columns {:style "display: none;"}
   [:div.column.is-one-quarter
    [:a#selected-image]]
   [:div#image-edit.column.is-half {:style "display: none;"}
    [:div.field
     [:label.label "Title"]
     [:div.control.has-icons-left [:input#title-nl.input {:type "text"}] [:span.icon.is-small.is-left "🇳🇱"]]]
    [:div.field
     [:label.label "Alt"]
     [:div.control.has-icons-left [:input#alt-nl.input {:type "text"}] [:span.icon.is-small.is-left "🇳🇱"]]]
    [:div.field
     [:div.control [:button#image-save-button.button.is-primary "Save"]]]]
   [:div.column [:div#all-images]]])

(defn content-edit
  []
  [:div#main-content-edit.field.is-grouped.is-grouped-multiline {:style "display: none;"}
   (button :start-menu-edit-button :1 "menu" false)
   (button :start-main-edit-button :1 "view-dashboard" false)
   (button :start-side-edit-button :1 "page-layout-sidebar-right" false)
   (button :add-page-button :1 "plus" false)
   (button :stop-edit-button :1 "stop" true)
   (button :verify-edit-button :1 "verified" true)
   (button :play-edit-button :1 "play" true)
   (button :save-edit-button :1 "content-save" true)])

(defn edit-bars
  []
  [:div.container
   (edit-buttons)
   (image-selection-columns)
   (content-edit)
   [:div#edit-box]])
