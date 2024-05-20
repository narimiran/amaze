(ns amaze.win-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw update-state key-press]]
   [amaze.config :refer [scene-width scene-height title-size text-size
                         margin line-height gold-multi bomb-multi]]
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
  (q/text "aMAZEing VICTORY" (/ scene-width 2) (/ scene-height 6)))


(defn- draw-score
  [{:keys [cnt walls win-time score-shown bombs-used picked-gold
           maze-best total-best]}]
  (let [right-pos  (- scene-width margin)
        wall-count (count walls)
        gold-count (count picked-gold)
        y1         (* 0.30 scene-height)
        [y1 y11 y2 y3 y4 y5]
        (range y1 600 line-height)
        y6         (+ y5 line-height 10)
        [y7 y8] (range (+ 50 y6) 600 line-height)]
    (q/text-size text-size)
    (q/text-style :normal)
    (q/text-align :left)
    (q/text "Walls:" margin y1)
    (q/text (str "Gold (x" gold-multi "):")
            margin y11)
    (q/text "Moves:" margin y2)
    (q/text "Time:" margin y3)
    (q/text (str "Bombs used (x" bomb-multi "):")
            margin y4)
    (q/rect margin y5 (- right-pos margin) 1)
    (q/text "Score:" margin y6)
    (q/text "Best score for this maze:" margin y7)
    (q/text "Best score ever:" margin y8)

    (q/text-align :right)
    (q/text wall-count right-pos y1)
    (q/text (* gold-multi gold-count) right-pos y11)
    (q/text (str "-" cnt) right-pos y2)
    (q/text (str "-" win-time) right-pos y3)
    (q/text (str (when (pos? bombs-used) "-")
                 (* bomb-multi bombs-used))
            right-pos y4)
    (q/text score-shown right-pos y6)
    (q/text maze-best right-pos y7)
    (q/text total-best right-pos y8)))


(defn- draw-keys []
  (let [y-pos (- scene-height 120)]
    (q/text-align :left)
    (q/text "N   create new maze" margin y-pos)
    (q/text "R   restart this maze" margin (+ y-pos line-height))))

(defmethod draw :win
  [state]
  (q/background 250)
  (q/fill 0)
  (draw-title)
  (draw-score state)
  (draw-keys))


(defn- update-score
  [{:keys [maze-best total-best score] :as state}]
  (-> state
      (assoc :total-best (max total-best score))
      (assoc :maze-best (max maze-best score))))

(defmethod update-state :win
  [{:keys [score score-shown] :as state}]
  ;; faster count-down initially
  (condp > score
    (- score-shown 50) (update state :score-shown - 11)
    score-shown        (update state :score-shown dec)
    (update-score state)))
