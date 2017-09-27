(ns m-venue.editor
  (:import [goog.editor.Command])
  (:require [goog.dom :as gdom]
            [goog.editor.Field :as field]
            [goog.editor.plugins.BasicTextFormatter :as basic-text-formatter]
            [goog.editor.plugins.EnterHandler :as enter-handler]
            [goog.editor.plugins.LinkBubble :as link-bubble]
            [goog.editor.plugins.LinkDialogPlugin :as link-dialog-plugin]
            [goog.editor.plugins.ListTabHandler :as list-tab-handler]
            [goog.editor.plugins.LoremIpsum :as lorem-ipsum]
            [goog.editor.plugins.UndoRedo :as undo-redo]
            [goog.editor.plugins.RemoveFormatting :as remove-formatting]
            [goog.editor.plugins.SpacesTabHandler :as spaces-tab-handler]
            [goog.ui.Toolbar :as toolbar]
            [goog.ui.ToolbarButton :as toolbar-button]
            [goog.ui.ToolbarRenderer :as toolbar-renderer]
            [goog.ui.ToolbarButtonRenderer :as toolbar-button-renderer]
            [goog.ui.editor.DefaultToolbar :as default-toolbar]
            [goog.ui.editor.ToolbarFactory :as toolbar-factory]
            [goog.ui.editor.ToolbarController :as toolbar-controller]
            [m-venue.util :as util]))

(def factory-map
  {:1 #(.makeToggleButton js/goog.ui.editor.ToolbarFactory %1 %2 %3 %4 %5 %6)
   :2 #(.undoRedoButtonFactory_ js/goog.ui.editor.DefaultToolbar %1 %2 %3 %4 %5 %6)
   :3 #(.fontColorFactory_ js/goog.ui.editor.DefaultToolbar %1 %2 %3 %4 %5 %6)
   :4 #(.backgroundColorFactory_ js/goog.ui.editor.DefaultToolbar %1 %2 %3 %4 %5 %6)
   :5 #(.makeButton js/goog.ui.editor.ToolbarFactory %1 %2 %3 %4 %5 %6)})

(def button-list [[goog.editor.Command.BOLD "is-primary" "fa-bold" true :1]
                  [goog.editor.Command.ITALIC "is-primary" "fa-italic" true :1]
                  [goog.editor.Command.UNDERLINE "is-primary" "fa-underline" true :1]
                  [goog.editor.Command.FONT_COLOR "is-primary is-inverted" "fa-font" true :3]
                  [goog.editor.Command.BACKGROUND_COLOR "is-primary" "fa-font" true :4]
                  [goog.editor.Command.LINK "is-primary" "fa-link" true :1]
                  [goog.editor.Command.UNDO "is-primary" "fa-undo" true :2]
                  [goog.editor.Command.REDO "is-primary" "fa-repeat" true :2]
                  [goog.editor.Command.UNORDERED_LIST "is-primary" "fa-list-ul" true :1]
                  [goog.editor.Command.ORDERED_LIST "is-primary" "fa-list-ol" true :1]
                  [goog.editor.Command.INDENT "is-primary" "fa-indent" false :5]
                  [goog.editor.Command.OUTDENT "is-primary" "fa-outdent" false :5]
                  [goog.editor.Command.JUSTIFY_LEFT "is-primary" "fa-align-left" true :1]
                  [goog.editor.Command.JUSTIFY_CENTER "is-primary" "fa-align-center" true :1]
                  [goog.editor.Command.JUSTIFY_RIGHT "is-primary" "fa-align-right" true :1]
                  [goog.editor.Command.REMOVE_FORMAT "is-danger" "fa-times" false :5]])

(defn button-array
  [dom-helper]
  (let[b-array #js []
       fill (doseq [[id b-class fa-class queryable factory]button-list]
              (let [button ((factory factory-map) id nil fa-class b-class nil dom-helper)]
                (if queryable (set! (.-queryable button) true))
                (. b-array (push button))))]
    b-array))

(defn make-tool-bar
  [id]
  [:div.field.is-grouped.is-grouped-multiline {:id id}
   (for [[goog-id b-class fa-class] button-list]
     [:p.control [:div {:id goog-id :class (str "button " b-class)}
                  [:span.icon [:i {:class (str "fa " fa-class)}]]]])])

(defn init! [field bar]
  (let [editField (goog.editor.Field. field)
        toolbar-element (util/ensure-element bar)
        button-array (button-array (gdom/getDomHelper toolbar-element))
        toolbar (default-toolbar/makeToolbar button-array toolbar-element)
        myToolbarController (goog.ui.editor.ToolbarController. editField toolbar)]
    (. editField (registerPlugin (goog.editor.plugins.BasicTextFormatter.)))
    (. editField (registerPlugin (goog.editor.plugins.RemoveFormatting.)))
    (. editField (registerPlugin (goog.editor.plugins.UndoRedo.)))
    (. editField (registerPlugin (goog.editor.plugins.ListTabHandler.)))
    (. editField (registerPlugin (goog.editor.plugins.SpacesTabHandler.)))
    (. editField (registerPlugin (goog.editor.plugins.EnterHandler.)))
    (. editField (registerPlugin (goog.editor.plugins.LinkDialogPlugin.)))
    (. editField (registerPlugin (goog.editor.plugins.LinkBubble.)))
    (. editField (makeEditable))))
