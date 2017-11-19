(ns m-venue.editor-templates)

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
   [:p.control [:button#add-page-button.button.is-primary.is-outlined
                [:span.icon [:i.mdi.mdi-24px.mdi-plus]]]]
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
     [:div.control.has-icons-left [:input#title-nl.input {:type "text"}][:span.icon.is-small.is-left "🇳🇱"]]]
    [:div.field
     [:label.label "Alt"]
     [:div.control.has-icons-left [:input#alt-nl.input {:type "text"}][:span.icon.is-small.is-left "🇳🇱"]]]
    [:div.field
     [:div.control [:button#image-save-button.button.is-primary "Save"]]]]
   [:div.column [:div#all-images]]])

(defn html-edit
  [id initial-value]
  [:div.notification {:id (str "html-edit-" id)}
   [:div.is-pulled-left {:id (str "toolbar-" id)}]
   [:div.field.is-grouped.is-grouped-multiline.is-pulled-right
    [:div.control {:style "display: none;" :id (str "html-paste-button-" id)}
     [:div.button.is-info [:span.icon [:i.mdi.mdi-24px.mdi-content-paste]]]]
    [:div.control [:div.button.is-info.is-outlined {:id (str "edit-html-button-" id)}[:span.icon [:i.mdi.mdi-24px.mdi-pencil]]]]]
   [:div {:style "width:100%;" :id (str "edit-me-" id)} initial-value]
   [:div {:style "display: none;" :id (str "html-paste-" id)}
    [:strong.is-pulled-left "Current field contents"]
    [:div.control.is-pulled-right [:button.button.is-primary {:id (str "set-field-contents-" id)} "Set Field Contents"]]
    [:br]
    [:textarea {:style "height:100px;width:100%;" :id (str "field-contents-" id)}]]])

(defn content-edit
  []
  [:div#main-content-edit.field.is-grouped.is-grouped-multiline {:style "display: none;"}
   [:p.control [:button#start-edit-button.button.is-primary {:disabled false}
                [:span.icon [:i.mdi.mdi-24px.mdi-pen]]]]
   [:p.control [:button#stop-edit-button.button.is-primary {:disabled true}
                [:span.icon [:i.mdi.mdi-24px.mdi-stop]]]]
   [:p.control [:button#verify-edit-button.button.is-primary {:disabled true}
                [:span.icon [:i.mdi.mdi-24px.mdi-verified]]]]
   [:p.control [:button#play-edit-button.button.is-primary {:disabled true}
                [:span.icon [:i.mdi.mdi-24px.mdi-play]]]]
   [:p.control [:button#save-edit-button.button.is-primary {:disabled true}
                [:span.icon [:i.mdi.mdi-24px.mdi-content-save]]]]])

(defn edit-bars
  []
  [:div.container
   (edit-buttons)
   (image-selection-columns)
   (content-edit)
   [:div#edit-box]])
