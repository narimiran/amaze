(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size width height free-pass generating-speed
                         text-size bottom-1 bottom-2 x1 gold-amount
                         background-color]]))


(defn- random-points [amount]
  (repeatedly amount
              (fn [_] [(inc (rand-int (- width 2)))
                       (inc (rand-int (- height 2)))])))

(defn- fade-out [start]
  (- 0.8
     (* 0.0001 (- (q/millis) start))))

(defn- create-vertical-walls [{:keys [scene-start]}]
  (let [x3      (quot width 3)
        y23     (quot (* 2 height) 3)
        scatter 5]
    ;; Initially focus more on the walls, then ease out and leave it to random.
    (when (< (rand) (fade-out scene-start))
      [[(+ x3 (rand-int scatter)) (inc (rand-int y23))]
       [(- width x3 (rand-int scatter)) (+ (quot y23 2) (rand-int y23))]])))

(defn- create-walls [state]
  (->> state
       create-vertical-walls
       (into (random-points generating-speed))
       (remove free-pass)))

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
                    (* 0.02 (- (q/millis) scene-start)))))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))


(defn- place-gold [walls]
  (->> (random-points 30)
       (remove walls)
       (take gold-amount)
       set))

(defmethod key-press :generation
  [{:keys [walls] :as state}]
  (case (q/key-as-keyword)
    :space (-> state
               (assoc :screen-type :navigation)
               (assoc :orig-walls walls)
               (assoc :gold (place-gold walls))
               (assoc :scene-start (q/millis)))
    :q     (-> state
               (assoc :screen-type :intro)
               (assoc :walls #{}))
    state))
