(ns amaze.win-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw update-state key-press]]
   [amaze.config :refer [scene-width scene-height title-size text-size]]
   [amaze.navigation :as nav]))


(defmethod key-press :win
  [state]
  (case (q/key-as-keyword)
    :r (nav/reset-level state)
    :n (nav/quit-level state)
    state))


(defn- draw-title []
  (q/text-size title-size)
  (q/text-align :center)
  (q/text-style :bold)
  (q/text "YOU WIN" (/ scene-width 2) (/ scene-height 4)))


(def left-pos 300)
(def line-height 20)

(defn- draw-score [{:keys [cnt walls win-time score-shown]}]
  (let [right-pos     (- scene-width left-pos)
        wall-count    (count walls)
        [y1 y2 y3 y4] (range 250 400 line-height)
        y5            (+ y4 line-height 10)]
    (q/text-size text-size)
    (q/text-style :normal)
    (q/text-align :left)
    (q/text "Walls:" left-pos y1)
    (q/text "Moves:" left-pos y2)
    (q/text "Time:" left-pos y3)
    (q/rect left-pos y4 (- right-pos left-pos) 1)
    (q/text "Score:" left-pos y5)
    (q/text-align :right)
    (q/text wall-count right-pos y1)
    (q/text cnt right-pos y2)
    (q/text win-time right-pos y3)
    (q/text score-shown right-pos y5)))

(defn- draw-keys []
  (let [y-pos 450]
    (q/text-align :left)
    (q/text "N   create new maze" left-pos y-pos)
    (q/text "R   restart this maze" left-pos (+ y-pos line-height))))

(defmethod draw :win
  [state]
  (q/background 240)
  (q/fill 0)
  (draw-title)
  (draw-score state)
  (draw-keys))


(defmethod update-state :win
  [{:keys [score score-shown] :as state}]
  (if (> score-shown score)
    (update state :score-shown dec)
    state))
