(ns amaze.intro-screen
  (:require
   [quil.core :as q]
   [amaze.navigation :as nav]
   [amaze.methods :refer [draw key-press]]
   [amaze.config :refer [title-size scene-width scene-height text-size]]))


(defn- draw-title []
  (q/text-size title-size)
  (q/text-align :center)
  (q/text-style :bold)
  (q/text "aMAZE" (/ scene-width 2) (* 0.3 scene-height)))

(defn- draw-text []
  (q/text-style :normal)
  (q/text-align :center)
  (q/text-size (* 1.3 text-size))
  (q/text "Press   SPACE   to play"
          (/ scene-width 2) (* 0.7 scene-height)))

(defmethod draw :intro
  [_state]
  (q/background 10)
  (q/fill 250)
  (draw-title)
  (draw-text))


(defmethod key-press :intro
  [state]
  (case (q/key-as-keyword)
    :space (nav/new-maze state)
    state))
