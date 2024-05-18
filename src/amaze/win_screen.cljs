(ns amaze.win-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw update-state key-press]]
   [amaze.config :refer [scene-width scene-height title-size text-size
                         margin line-height]]
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


(defn- draw-score [{:keys [cnt walls win-time score-shown]}]
  (let [right-pos     (- scene-width margin)
        wall-count    (count walls)
        y1            (* 0.4 scene-height)
        [y1 y2 y3 y4] (range y1 600 line-height)
        y5            (+ y4 line-height 10)]
    (q/text-size text-size)
    (q/text-style :normal)
    (q/text-align :left)
    (q/text "Walls:" margin y1)
    (q/text "Moves:" margin y2)
    (q/text "Time:" margin y3)
    (q/rect margin y4 (- right-pos margin) 1)
    (q/text "Score:" margin y5)
    (q/text-align :right)
    (q/text wall-count right-pos y1)
    (q/text (str "-" cnt) right-pos y2)
    (q/text (str "-" win-time) right-pos y3)
    (q/text score-shown right-pos y5)))

(defn- draw-keys []
  (let [y-pos (- scene-height 200)]
    (q/text-align :left)
    (q/text "N   create new maze" margin y-pos)
    (q/text "R   restart this maze" margin (+ y-pos line-height))))

(defmethod draw :win
  [state]
  (q/background 240)
  (q/fill 0)
  (draw-title)
  (draw-score state)
  (draw-keys))


(defmethod update-state :win
  [{:keys [score score-shown] :as state}]
  ;; faster count-down initially
  (condp > score
    (- score-shown 30) (update state :score-shown - 11)
    score-shown        (update state :score-shown dec)
    state))
