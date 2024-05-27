(ns amaze.navigation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press key-release change-screen]]
   [amaze.config :refer [size start finish gold-multi gold-amount
                         bomb-multi bomb-limit width height
                         text-size x1 x2 x3 x4 x5 bottom-1 bottom-2
                         background-color]]))


(defn reset-level [state]
  (change-screen state :navigation
                 {:walls       (:orig-walls state)
                  :bombs-used  0
                  :bomb-expls  []
                  :pos         start
                  :path        [start]
                  :picked-gold #{}
                  :moves       0}))

(defn quit-level [state]
  (-> state
      reset-level
      (change-screen :generation
                     {:walls #{}
                      :maze-best 0})))

(defn- calc-score [{:keys [moves walls win-time bombs-used picked-gold]}]
  ;; It counts nr. of walls after bombs; not `orig-walls`.
  ;; Not what I originally intended, but let it stay this way now.
  (- (+ (count walls) (* gold-multi (count picked-gold)))
     moves win-time (* bomb-multi bombs-used)))

(defn- pt+ [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn- check-and-move
  [{:keys [pos borders walls] :as state} delta]
  (let [new-pos (pt+ pos delta)]
    (if (or (borders new-pos)
            (walls new-pos))
      state
      (-> state
          (assoc :pos new-pos)
          (update :path conj new-pos)
          (update :moves inc)))))

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

(defn- handle-keys [state]
  (reduce
   make-move
   state
   (:keys-held state)))

(defmethod update-state :navigation
  [{:keys [pos calc-duration scene-start walls gold picked-gold keys-held]
    :as state}]
  (cond
    (= pos finish)
    (let [win-time (calc-duration scene-start)
          state    (assoc state :win-time win-time)
          score    (calc-score state)]
      (change-screen state :win
                     {:score       score
                      :score-shown (count walls)}))

    (and (gold pos)
         (not (picked-gold pos)))
    (update state :picked-gold conj pos)

    (some? keys-held)
    (handle-keys state)

    :else
    state))


(defn draw-obstacles
  [{:keys [borders walls]}]
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))

(defn- draw-player [{:keys [pos calc-duration scene-start path]}]
  (let [[x-pos y-pos] pos]
    (q/fill 50 150 90)
    (if (and (< (calc-duration scene-start) 3)
             (<= (count path) 1)
             (= pos start))
      (let [r (inc (mod (/ (q/millis) 300) 2))]
        (q/ellipse-mode :center)
        (q/ellipse (+ 0.5 x-pos) (+ 0.5 y-pos) r r)
        (q/ellipse-mode :corner))
      (q/ellipse x-pos y-pos 1 1))))

(defn- draw-text
  [{:keys [moves calc-duration scene-start walls bombs-used picked-gold]}]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)

  (q/text (str "Walls: " (count walls)) x1 bottom-1)
  (q/text (str "Time: " (calc-duration scene-start))
          x2 bottom-1)
  (q/text (str "Bombs used: " bombs-used "/" bomb-limit) x3 bottom-1)
  (q/text (str "Gold taken: " (count picked-gold) "/" gold-amount) x4 bottom-1)
  (q/text (str "Moves: " moves) x5 bottom-1)

  (q/text "SPACE  drop bomp" x1 bottom-2)
  (q/text "N  new maze" x2 bottom-2)
  (q/text "R  restart maze" x3 bottom-2))

(defn draw-gold [{:keys [gold picked-gold]} remove-picked?]
  (let [visible-gold (if remove-picked? (remove picked-gold gold) gold)]
    (q/fill 255 230 0)
    (let [[dx1 dx2 dx3 dx4] [0.5 1.0 0.5 0.0]
          [dy1 dy2 dy3 dy4] [-0.3 0.5 1.3 0.5]]
      (doseq [[x y] visible-gold]
        (if (zero? (mod (quot (q/millis) 500) 2))
          (q/quad (+ x dx1) (+ y dy1)
                  (+ x dx2) (+ y dy2)
                  (+ x dx3) (+ y dy3)
                  (+ x dx4) (+ y dy4))
          (q/quad (+ x dy1) (+ y dx1)
                  (+ x dy2) (+ y dx2)
                  (+ x dy3) (+ y dx3)
                  (+ x dy4) (+ y dx4)))))))

(defn- draw-bomb-explosion [{:keys [bomb-loc bomb-time]}]
  (when (<= (q/millis) (+ 300 bomb-time))
    (q/fill (+ 200 (rand-int 55))
            (+ 150 (rand-int 105))
            (+ 50 (rand-int 155)))
    (doseq [[x y] bomb-loc]
      (q/rect (+ 0.15 x) (+ 0.15 y) 0.7 0.7))))

(defmethod draw :navigation
  [state]
  (q/background background-color)
  (draw-text state)
  (q/fill 0)
  (q/scale size)
  (draw-obstacles state)
  (draw-gold state true)
  (draw-bomb-explosion state)
  (draw-player state))


(defn- deploy-bomb
  [{:keys [walls bombs-used]
    [x y] :pos :as state}]
  (if (>= bombs-used bomb-limit)
    state
    (let [power  2
          nbs    (for [nbx   (range (- power) (inc power))
                       nby   (range (- power) (inc power))
                       :let  [x' (+ x nbx)
                              y' (+ y nby)]
                       :when (and (<= (+ (abs nbx) (abs nby)) power)
                                  (< 0 x' (dec width))
                                  (< 0 y' (dec height)))]
                   [x' y'])
          walls' (apply disj (:walls state) nbs)]
      (if (not= walls' walls)
        (-> state
            (assoc :bomb-loc nbs)
            (assoc :bomb-time (q/millis))
            (update :bombs-used inc)
            (update :bomb-expls into nbs)
            (assoc :walls walls'))
        state))))

(defmethod key-press :navigation
  [state]
  (let [k (q/key-as-keyword)]
    (case k
      :space (deploy-bomb state)
      :r     (reset-level state)
      :n     (quit-level state)
      (update state :keys-held conj k))))

(defmethod key-release :navigation
  [state e]
  (update state :keys-held disj (:key e)))
