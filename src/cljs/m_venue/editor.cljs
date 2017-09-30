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

(def button-list [[Command/BOLD "is-primary" "fa-bold" true :1]
                  [Command/ITALIC "is-primary" "fa-italic" true :1]
                  [Command/UNDERLINE "is-primary" "fa-underline" true :1]
                  [Command/FONT_COLOR "is-primary is-inverted" "fa-font" true :3]
                  [Command/BACKGROUND_COLOR "is-primary" "fa-font" true :4]
                  [Command/LINK "is-primary" "fa-link" true :1]
                  [Command/UNDO "is-primary" "fa-undo" true :2]
                  [Command/REDO "is-primary" "fa-repeat" true :2]
                  [Command/UNORDERED_LIST "is-primary" "fa-list-ul" true :1]
                  [Command/ORDERED_LIST "is-primary" "fa-list-ol" true :1]
                  [Command/INDENT "is-primary" "fa-indent" false :5]
                  [Command/OUTDENT "is-primary" "fa-outdent" false :5]
                  [Command/JUSTIFY_LEFT "is-primary" "fa-align-left" true :1]
                  [Command/JUSTIFY_CENTER "is-primary" "fa-align-center" true :1]
                  [Command/JUSTIFY_RIGHT "is-primary" "fa-align-right" true :1]
                  [Command/REMOVE_FORMAT "is-danger" "fa-times" false :5]])

(defn get-icon-span
  [fa-class]
  (util/node-from-data [:span.icon [:i {:class (str "fa " fa-class)}]])
  )

(defn button-array
  [dom-helper]
  (mapv (fn [[id b-class fa-class queryable factory]]
          (let [button ((factory factory-map) id nil (get-icon-span fa-class) (str "button " b-class) nil dom-helper)]
            (if queryable (set! (.-queryable button) true))
            (.setSupportedState button gstate/FOCUSED false)
            (.setRightToLeft button false)
            button))
        button-list))

(defn make-toolbar
  [button-array toolbar-element dom-helper]
  (let [toolbar-r (container-renderer/getCustomRenderer goog.ui.ToolbarRenderer. "field is-grouped is-grouped-multiline")
        toolbar (Toolbar. toolbar-r gorientation/HORIZONTAL dom-helper)]
    (set! (.-rightToLeft_ toolbar) false)
    (.setRightToLeft toolbar false)
    (.setFocusable toolbar false)
    (doseq [button button-array]
      (.addChild toolbar button true))
    (.render toolbar toolbar-element)
    toolbar))

(defn init! [field bar]
  (let [editField (Field. field)
        toolbar-element (util/ensure-element bar)
        dom-helper (gdom/getDomHelper toolbar-element)
        button-array (button-array dom-helper)
        toolbar (make-toolbar button-array toolbar-element dom-helper)
        myToolbarController (ToolbarController. editField toolbar)]
    (.registerPlugin editField (BasicTextFormatter.))
    (.registerPlugin editField (EnterHandler.))
    (.registerPlugin editField (LinkBubble.))
    (.registerPlugin editField (LinkDialogPlugin.))
    (.registerPlugin editField (ListTabHandler.))
    (.registerPlugin editField (RemoveFormatting.))
    (.registerPlugin editField (SpacesTabHandler.))
    (.registerPlugin editField (UndoRedo.))
    (.makeEditable editField)))
