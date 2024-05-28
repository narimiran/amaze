(ns amaze.win-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw update-state key-press]]
   [amaze.config :refer [scene-width scene-height title-size text-size size
                         left-margin right-margin line-height
                         gold-multi bomb-multi time-multi
                         width height background-color]]
   [amaze.navigation :as nav]))


(defmethod key-press :win
  [state]
  (case (q/key-as-keyword)
    :r (nav/reset-level state)
    :n (nav/quit-level state)
    state))


(defn- draw-title [{:keys [score score-shown]}]
  (q/text-size title-size)
  (q/text-align :right)
  (q/text-style :bold)
  (let [txt (if (and (= score score-shown)
                     (>= score 777))
              "aMAZEing VICTORY"
              "VICTORY")]
    (q/text txt (- scene-width right-margin) (* 0.1 scene-height))))

(defn- draw-score
  [{:keys [moves walls win-time score-shown bombs-used picked-gold
           maze-best total-best]}]
  (let [right-pos  (- scene-width right-margin)
        wall-count (count walls)
        gold-count (count picked-gold)
        y1         (* 0.20 scene-height)
        [y1 y11 y2 y3 y4 y5]
        (range y1 600 line-height)
        y6         (+ y5 line-height 10)
        [y7 y8]    (range (+ 50 y6) 600 line-height)]
    (q/text-size text-size)
    (q/text-style :normal)
    (q/text-align :left)
    (q/text "Walls:" left-margin y1)
    (q/text (str "Gold (x" gold-multi "):")
            left-margin y11)
    (q/text "Moves:" left-margin y2)
    (q/text (str "Time (x" time-multi "):") left-margin y3)
    (q/text (str "Bombs used (x" bomb-multi "):")
            left-margin y4)
    (q/rect left-margin y5 (- right-pos left-margin) 1)
    (q/text "Score:" left-margin y6)
    (q/text "Best score for this maze:" left-margin y7)
    (q/text "Best score ever:" left-margin y8)

    (q/text-align :right)
    (q/text wall-count right-pos y1)
    (q/text (* gold-multi gold-count) right-pos y11)
    (q/text (str "-" moves) right-pos y2)
    (q/text (str "-" (* time-multi win-time)) right-pos y3)
    (q/text (str (when (pos? bombs-used) "-")
                 (* bomb-multi bombs-used))
            right-pos y4)
    (q/text score-shown right-pos y6)
    (q/text maze-best right-pos y7)
    (q/text total-best right-pos y8)))


(defn- draw-keys []
  (let [y-pos (- scene-height 200)]
    (q/text-align :left)
    (q/text "N   create new maze" left-margin y-pos)
    (q/text "R   restart this maze" left-margin (+ y-pos line-height))))

(defn- draw-mini-map
  [{:keys [path bomb-expls] :as state}]
  (q/scale (* 0.5 size))
  (q/translate 2 2)
  (q/fill background-color)
  (q/rect 0 0 width height)
  (q/fill 0)
  (nav/draw-obstacles state)
  (q/fill 50 150 90)
  (doseq [[x y] path]
    (q/rect x y 1 1))
  (q/fill 240 80 20)
  (doseq [[x y] bomb-expls]
    (q/rect (+ 0.1 x) (+ 0.1 y) 0.8 0.8))
  (nav/draw-gold state false))

(defmethod draw :win
  [state]
  (q/background 250)
  (q/fill 0)
  (draw-title state)
  (draw-score state)
  (draw-keys)
  (draw-mini-map state))

(defn- update-total-best [{:keys [total-best score] :as state}]
  (if (> score total-best)
    (do
      (.. js/window -localStorage (setItem :high-score score))
      (assoc state :total-best score))
    state))

(defn- update-score
  [{:keys [maze-best score] :as state}]
  (-> state
      update-total-best
      (assoc :maze-best (max maze-best score))))

(defmethod update-state :win
  [{:keys [score score-shown] :as state}]
  ;; faster count-down initially
  (condp > score
    (- score-shown 50) (update state :score-shown - 11)
    score-shown        (update state :score-shown dec)
    (update-score state)))
