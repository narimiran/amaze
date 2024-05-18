(ns amaze.navigation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size start finish scene-height text-size]]))


(defn reset-level [state]
  (-> state
      (assoc :screen-type :navigation)
      (assoc :pos start)
      (assoc :scene-start (q/millis))
      (assoc :cnt 0)))

(defn quit-level [state]
  (-> state
      reset-level
      (assoc :walls #{})
      (assoc :scene-start (q/millis))
      (assoc :screen-type :intro)))

(defn- calc-score [{:keys [cnt walls win-time]}]
  (- (count walls) cnt win-time))

(defmethod update-state :navigation
  [{:keys [pos calc-duration scene-start walls] :as state}]
  (if (= pos finish)
    (let [win-time (calc-duration scene-start)
          state    (assoc state :win-time win-time)
          score    (calc-score state)]
      (-> state
          (assoc :score score)
          (assoc :score-shown (count walls))
          (assoc :screen-type :win)))
    state))


(defn- draw-obstacles
  [{:keys [borders walls]}]
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))

(defn- draw-player [[x-pos y-pos]]
  (q/fill 80 200 170)
  (q/ellipse x-pos y-pos 1 1))

(defn- draw-text
  [{:keys [cnt calc-duration scene-start pos]}]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text (str "Time elapsed: " (calc-duration scene-start)) 10 (- scene-height 32))
  (q/text (str "Moves: " cnt) 10 (- scene-height 10)))

(defmethod draw :navigation
  [state]
  (q/background 200)
  (q/fill 40)
  (draw-text state)
  (q/scale size)
  (draw-obstacles state)
  (draw-player (:pos state)))


(defn- pt+ [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn- check-and-move
  [{:keys [pos borders walls] :as state} delta]
  (let [new-pos (pt+ pos delta)]
    (if (or (contains? borders new-pos)
            (contains? walls new-pos)
            (= new-pos (pt+ start [0 -1])))
      state
      (-> state
          (assoc :pos new-pos)
          (update :cnt inc)))))

(defmethod key-press :navigation
  [state]
  (case (q/key-as-keyword)
    (:w :ArrowUp)    (check-and-move state [ 0 -1])
    (:s :ArrowDown)  (check-and-move state [ 0  1])
    (:a :ArrowLeft)  (check-and-move state [-1  0])
    (:d :ArrowRight) (check-and-move state [ 1  0])
    :r               (reset-level state)
    :n               (quit-level state)
    state))
