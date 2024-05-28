(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press change-screen]]
   [amaze.config :refer [size width height free-area
                         text-size bottom-1 bottom-2 x1 gold-amount
                         background-color start finish]]))


(defn- random-point []
  [(inc (rand-int (- width 2)))
   (inc (rand-int (- height 2)))])

(defn- pt+ [a b]
  (mapv + a b))

(defn- create-maze []
  (let [[sx sy] (random-point)
        start [(- sx (mod sx 2)) (- sy (mod sy 2))]
        walls (set (for [x (range 1 (dec width))
                         y (range 1 (dec height))]
                     [x y]))]
    (loop [stack   (list start)
           visited #{start}
           walls   walls]
      (if (empty? stack)
        walls
        (let [curr (peek stack)
              nbs  (shuffle (for [delta [[1 0] [-1 0] [0 1] [0 -1]]
                                  :let  [mid (pt+ curr delta)
                                         [nbx nby :as nb] (pt+ mid delta)]
                                  :when (and (not (visited nb))
                                             (not (visited mid))
                                             (< 0 nbx (dec width))
                                             (< 0 nby (dec height)))]
                              [mid nb]))]
          (if-let [[mid nb] (first nbs)]
            (recur (conj stack nb)
                   (conj visited curr mid nb)
                   (disj walls curr mid nb))
            (recur (pop stack)
                   (conj visited curr)
                   walls)))))))

(defn- pick-neighbours [maze [px py]]
  (for [x (range (dec px) (inc (inc px)))
        y (range (dec py) (inc (inc py)))
        :when (maze [x y])]
    [x y]))


(defn- pick-from-maze [maze]
  (loop [p      (random-point)
         picked #{}]
    (cond
      (>= (count picked) 10) picked
      (maze p)               (recur (random-point) (into picked (pick-neighbours maze p)))
      :else                  (recur (random-point) picked))))


(def free-pass
  (let [[sx sy] start
        [fx fy] finish]
    (->> (for [dx (range free-area)
               dy (range free-area)]
           [[(+ sx dx) (+ sy dy)]
            [(- fx dx) (- fy dy)]])
         (apply concat)
         set)))

(def maze (create-maze))

(defn- create-walls [state]
  (->> (pick-from-maze maze)
       (remove free-pass)
       (remove (:borders state))))

(defmethod update-state :generation
  [state]
  (update state :walls into (create-walls state)))


(defn- draw-text [walls]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text (str "Walls: " (count walls)) x1 bottom-1)
  (q/text "SPACE  stop generating walls" x1 bottom-2))

(defmethod draw :generation
  [{:keys [borders walls scene-start]}]
  (q/background background-color)
  (draw-text walls)
  (q/scale size)
  (q/fill 0)
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (q/fill (max 0 (- background-color
                    (* 0.04 (- (q/millis) scene-start)))))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))


(defn- place-gold [walls]
  (->> (repeatedly 30 random-point)
       (remove walls)
       (take gold-amount)
       set))

(defmethod key-press :generation
  [{:keys [walls] :as state}]
  (case (q/key-as-keyword)
    (:space :n) (change-screen state :navigation
                               {:orig-walls walls
                                :gold       (place-gold walls)})
    :q          (change-screen state :intro
                               {:walls #{}})
    state))
