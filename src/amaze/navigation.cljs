(ns amaze.navigation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size start finish scene-width
                         text-size left-x bottom-1 bottom-2]]))


(defn reset-level [state]
  (-> state
      (assoc :screen-type :navigation)
      (assoc :walls (:orig-walls state))
      (assoc :bombs-used 0)
      (assoc :pos start)
      (assoc :scene-start (q/millis))
      (assoc :cnt 0)))

(defn quit-level [state]
  (-> state
      reset-level
      (assoc :walls #{})
      (assoc :bombs-used 0)
      (assoc :scene-start (q/millis))
      (assoc :screen-type :generation)))

(defn- calc-score [{:keys [cnt walls win-time bombs-used]}]
  (- (count walls) cnt win-time bombs-used))

(defn- pt+ [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn- check-and-move
  [{:keys [pos borders walls] :as state} delta]
  (let [new-pos (pt+ pos delta)]
    (if (or (borders new-pos)
            (walls new-pos)
            (= new-pos (pt+ start [0 -1])))
      state
      (-> state
          (assoc :pos new-pos)
          (update :cnt inc)))))

(defn- make-move
  "A workaround to make repeated moves when a keys is held longer.
  Using `:key-pressed`, i.e. the `key-press` method, would move only once."
  [state k]
  (case k
    (:w :ArrowUp)    (check-and-move state [ 0 -1])
    (:s :ArrowDown)  (check-and-move state [ 0  1])
    (:a :ArrowLeft)  (check-and-move state [-1  0])
    (:d :ArrowRight) (check-and-move state [ 1  0])
    state))

(defmethod update-state :navigation
  [{:keys [pos calc-duration scene-start walls] :as state}]
  (cond
    (= pos finish)   (let [win-time (calc-duration scene-start)
                           state    (assoc state :win-time win-time)
                           score    (calc-score state)]
                       (-> state
                           (assoc :score score)
                           (assoc :score-shown (count walls))
                           (assoc :screen-type :win)))
    (q/key-pressed?) (make-move state (q/key-as-keyword))
    :else            state))


(defn- draw-obstacles
  [{:keys [borders walls]}]
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))

(defn- draw-player [[x-pos y-pos]]
  (q/fill 50 150 90)
  (q/ellipse x-pos y-pos 1 1))

(defn- draw-text
  [{:keys [cnt calc-duration scene-start]}]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text (str "Time elapsed: " (calc-duration scene-start)) left-x bottom-1)
  (q/text (str "Moves: " cnt) left-x bottom-2)
  (q/text "N   create new maze" (quot scene-width 2) bottom-1)
  (q/text "R   restart this maze" (quot scene-width 2) bottom-2))

(defmethod draw :navigation
  [state]
  (q/background 200)
  (draw-text state)
  (q/fill 0)
  (q/scale size)
  (draw-obstacles state)
  (draw-player (:pos state)))



(defn- deploy-bomb
  [{[x y] :pos :as state}]
  (let [power 2
        nbs (for [nbx (range (- power) (inc power))
                  nby (range (- power) (inc power))
                  :when (<= (+ (abs nbx) (abs nby)) power)]
              [(+ x nbx) (+ y nby)])
        new-walls (apply disj (:walls state) nbs)]
    (-> state
        (update :bombs-used inc)
        (assoc :walls new-walls))))



(defmethod key-press :navigation
  [state]
  (case (q/key-as-keyword)
    :space (deploy-bomb state)
    :r     (reset-level state)
    :n     (quit-level state)
    state
    ))
