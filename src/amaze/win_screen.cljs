(ns amaze.win-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw key-press]]
   [amaze.config :refer [scene-width scene-height title-size text-size]]
   [amaze.navigation :as nav]))


(defmethod key-press :win
  [state]
  (case (q/key-as-keyword)
    :r    (nav/reset-level state)
    :q    (nav/quit-level state)
    :else state))


(defn- draw-title []
  (q/text-size title-size)
  (q/text-align :center)
  (q/text "YOU WIN" (/ scene-width 2) (/ scene-height 4)))

(defn- draw-score [{:keys [cnt walls win-time]}]
  (let [left-margin 300
        right-pos   (- scene-width left-margin)
        wall-count  (count walls)
        score       (- wall-count cnt win-time)]
    (q/text-size text-size)
    (q/text-align :left)
    (q/text "Walls:" left-margin 300)
    (q/text "Moves:" left-margin 320)
    (q/text "Time:" left-margin 340)
    (q/rect left-margin 360 (- right-pos left-margin) 1)
    (q/text "Score:" left-margin 390)
    (q/text-align :right)
    (q/text wall-count right-pos 300)
    (q/text cnt right-pos 320)
    (q/text win-time right-pos 340)
    (q/text score right-pos 390)))

(defmethod draw :win
  [state]
  (q/background 240)
  (q/fill 0)
  (draw-title)
  (draw-score state))
