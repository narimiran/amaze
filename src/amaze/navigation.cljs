(ns amaze.navigation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size start finish gold-multi gold-amount
                         bomb-multi bomb-limit width height
                         text-size x1 x2 x3 x4 x5 bottom-1 bottom-2
                         background-color move-timeout]]))


(defn reset-level [state]
  (-> state
      (assoc :screen-type :navigation)
      (assoc :walls (:orig-walls state))
      (assoc :bombs-used 0)
      (assoc :pos start)
      (assoc :picked-gold #{})
      (assoc :scene-start (q/millis))
      (assoc :cnt 0)))

(defn quit-level [state]
  (-> state
      reset-level
      (assoc :walls #{})
      (assoc :maze-best 0)
      (assoc :screen-type :generation)))

(defn- calc-score [{:keys [cnt walls win-time bombs-used picked-gold]}]
  (- (+ (count walls) (* gold-multi (count picked-gold)))
     cnt win-time (* bomb-multi bombs-used)))

(defn- pt+ [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn- check-and-move
  [{:keys [pos borders walls] :as state} delta]
  (let [new-pos (pt+ pos delta)]
    (if (or (borders new-pos)
            (walls new-pos)
            (< (q/millis) (+ (:last-move state 0) move-timeout)))
      state
      (-> state
          (assoc :pos new-pos)
          (assoc :last-move (q/millis))
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
  [{:keys [pos calc-duration scene-start walls gold picked-gold] :as state}]
  (cond
    (= pos finish)
    (let [win-time (calc-duration scene-start)
          state    (assoc state :win-time win-time)
          score    (calc-score state)]
      (-> state
          (assoc :score score)
          (assoc :score-shown (count walls))
          (assoc :screen-type :win)))

    (and (gold pos)
         (not (picked-gold pos)))
    (update state :picked-gold conj pos)

    (q/key-pressed?)
    (make-move state (q/key-as-keyword))

    :else
    state))


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
  [{:keys [cnt calc-duration scene-start walls bombs-used picked-gold]}]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)

  (q/text (str "Walls: " (count walls)) x1 bottom-1)
  (q/text (str "Time: " (calc-duration scene-start))
          x2 bottom-1)
  (q/text (str "Bombs used: " bombs-used "/" bomb-limit) x3 bottom-1)
  (q/text (str "Gold taken: " (count picked-gold) "/" gold-amount) x4 bottom-1)
  (q/text (str "Moves: " cnt) x5 bottom-1)

  (q/text "SPACE  drop bomp" x1 bottom-2)
  (q/text "N  new maze" x2 bottom-2)
  (q/text "R  restart maze" x3 bottom-2))

(defn- draw-gold [{:keys [gold picked-gold]}]
  (let [visible-gold (remove picked-gold gold)]
    (q/fill 255 230 0)
    (doseq [[x y] visible-gold]
      (q/quad (+ x 0.5) (- y 0.2)
              (+ x 0.9) (+ y 0.5)
              (+ x 0.5) (+ y 1.2)
              (+ x 0.1) (+ y 0.5)))))

(defn- draw-bomb-explosion [{:keys [bomb-loc bomb-time]}]
  (when (<= (q/millis) (+ 300 bomb-time))
    (q/fill (+ 200 (rand-int 55))
            (+ 150 (rand-int 105))
            (+ 50 (rand-int 155)))
    (doseq [[x y] bomb-loc
            :when (and (< 0 x width)
                       (< 0 y height))]
      (q/rect (+ 0.15 x) (+ 0.15 y) 0.7 0.7))))

(defmethod draw :navigation
  [state]
  (q/background background-color)
  (draw-text state)
  (q/fill 0)
  (q/scale size)
  (draw-obstacles state)
  (draw-gold state)
  (draw-bomb-explosion state)
  (draw-player (:pos state)))


(defn- deploy-bomb
  [{:keys [walls bombs-used]
    [x y] :pos :as state}]
  (if (>= bombs-used bomb-limit)
    state
    (let [power  2
          nbs    (for [nbx   (range (- power) (inc power))
                       nby   (range (- power) (inc power))
                       :when (<= (+ (abs nbx) (abs nby)) power)]
                   [(+ x nbx) (+ y nby)])
          walls' (apply disj (:walls state) nbs)]
      (if (not= walls' walls)
        (-> state
            (assoc :bomb-loc nbs)
            (assoc :bomb-time (q/millis))
            (update :bombs-used inc)
            (assoc :walls walls'))
        state))))

(defmethod key-press :navigation
  [state]
  (case (q/key-as-keyword)
    :space (deploy-bomb state)
    :r     (reset-level state)
    :n     (quit-level state)
    state))
