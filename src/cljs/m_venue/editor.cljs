(ns m-venue.editor
  (:import goog.editor.Field
           goog.editor.plugins.BasicTextFormatter
           goog.editor.plugins.EnterHandler
           goog.editor.plugins.LinkBubble
           goog.editor.plugins.LinkDialogPlugin
           goog.editor.plugins.ListTabHandler
           goog.editor.plugins.UndoRedo
           goog.editor.plugins.RemoveFormatting
           goog.editor.plugins.SpacesTabHandler
           goog.ui.editor.ToolbarController
           goog.ui.Toolbar)
  (:require [goog.dom :as gdom]
            [goog.editor.Command :as Command]
            [goog.html.legacyconversions :as legacy]
            [goog.ui.Component.State :as gstate]
            [goog.ui.Container.Orientation :as gorientation]
            [goog.ui.ContainerRenderer :as container-renderer]
            [goog.ui.editor.DefaultToolbar :as default-toolbar]
            [goog.ui.editor.ToolbarFactory :as toolbar-factory]
            [m-venue.util :as util]))

(def factory-map
  {:1 #(toolbar-factory/makeToggleButton %1 %2 %3 %4 %5 %6)
   :2 #(default-toolbar/undoRedoButtonFactory_ %1 %2 %3 %4 %5 %6)
   :3 #(default-toolbar/fontColorFactory_ %1 %2 %3 %4 %5 %6)
   :4 #(default-toolbar/backgroundColorFactory_ %1 %2 %3 %4 %5 %6)
   :5 #(toolbar-factory/makeButton %1 %2 %3 %4 %5 %6)})

(def button-list [[Command/BOLD "is-primary" "mdi-format-bold" true :1]
                  [Command/ITALIC "is-primary" "mdi-format-italic" true :1]
                  [Command/UNDERLINE "is-primary" "mdi-format-underline" true :1]
                  [Command/FONT_COLOR "is-primary" "mdi-format-color-text" true :3]
                  [Command/BACKGROUND_COLOR "is-primary" "mdi-format-color-fill" true :4]
                  [Command/LINK "is-primary" "mdi-link-variant" true :1]
                  [Command/UNDO "is-primary" "mdi-undo" true :2]
                  [Command/REDO "is-primary" "mdi-redo" true :2]
                  [Command/UNORDERED_LIST "is-primary" "mdi-format-list-bulleted" true :1]
                  [Command/ORDERED_LIST "is-primary" "mdi-format-list-numbers" true :1]
                  [Command/INDENT "is-primary" "mdi-format-indent-increase" false :5]
                  [Command/OUTDENT "is-primary" "mdi-format-indent-decrease" false :5]
                  [Command/JUSTIFY_LEFT "is-primary" "mdi-format-align-left" true :1]
                  [Command/JUSTIFY_CENTER "is-primary" "mdi-format-align-center" true :1]
                  [Command/JUSTIFY_RIGHT "is-primary" "mdi-format-align-right" true :1]
                  [Command/REMOVE_FORMAT "is-danger" "mdi-format-clear" false :5]])

(defn get-icon-span
  [mdi-class]
  (util/node-from-data [:span.icon [:i {:class (str "mdi mdi-24px " mdi-class)}]]))

(defn button-array
  [dom-helper]
  (mapv (fn [[id b-class mdi-class queryable factory]]
          (let [button ((factory factory-map) id nil (get-icon-span mdi-class) (str "button " b-class) nil dom-helper)]
            (if queryable (set! (.-queryable button) true))
            (.setSupportedState button gstate/FOCUSED false)
            (.setRightToLeft button false)
            button))
        button-list))

(defn make-toolbar
  [button-array toolbar-element dom-helper]
  (let [toolbar-r (container-renderer/getCustomRenderer goog.ui.ToolbarRenderer. "field is-grouped is-grouped-multiline is-pulled-left")
        toolbar (Toolbar. toolbar-r gorientation/HORIZONTAL dom-helper)]
    (set! (.-rightToLeft_ toolbar) false)
    (.setRightToLeft toolbar false)
    (.setFocusable toolbar false)
    (doseq [button button-array]
      (.addChild toolbar button true))
    (.render toolbar toolbar-element)
    toolbar))

(defn editable-switch
  [edit-html-button editField bar html-paste-button]
  (util/toggle-visibility html-paste-button)
  (util/toggle-visibility bar)
  (util/toggle-class edit-html-button "is-outlined")
  (if (.isUneditable editField)
    (.makeEditable editField)
    (.makeUneditable editField)))

(defn init! [id]
  (let [editField (Field. (str "edit-me-" id))
        toolbar-element (util/ensure-element (str "toolbar-" id))
        dom-helper (gdom/getDomHelper toolbar-element)
        button-array (button-array dom-helper)
        toolbar (make-toolbar button-array toolbar-element dom-helper)
        myToolbarController (ToolbarController. editField toolbar)
        update-function #(set! (.-value (util/ensure-element (str "field-contents-" id))) (.getCleanContents editField))
        edit-html-button (util/ensure-element (str "edit-html-button-" id))
        html-paste-button (util/ensure-element (str "html-paste-button-" id))]
    (.registerPlugin editField (BasicTextFormatter.))
    (.registerPlugin editField (EnterHandler.))
    (.registerPlugin editField (LinkBubble.))
    (.registerPlugin editField (LinkDialogPlugin.))
    (.registerPlugin editField (ListTabHandler.))
    (.registerPlugin editField (RemoveFormatting.))
    (.registerPlugin editField (SpacesTabHandler.))
    (.registerPlugin editField (UndoRedo.))
    (util/on-delayed-change editField update-function)
    (util/on-click (str "set-field-contents-" id) #(.setSafeHtml editField false (legacy/safeHtmlFromString (.-value (util/ensure-element (str "field-contents-" id)))) false))
    (.makeEditable editField)
    (update-function)
    (util/toggle-visibility html-paste-button)
    (util/toggle-class edit-html-button "is-outlined")
    (util/on-click edit-html-button #(editable-switch edit-html-button editField toolbar-element html-paste-button))
    (util/on-click html-paste-button #(util/toggle-visibility (str "html-paste-" id)))))
