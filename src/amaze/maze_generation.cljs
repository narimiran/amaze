(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size width height free-pass generating-speed
                         text-size scene-height]]))


(defn- create-rand-walls []
  (->> (repeatedly generating-speed
                   (fn [_] [(rand-int width) (rand-int height)]))
       (remove free-pass)))

(defn- create-vertical-walls []
  (let [x3 (quot width 3)
        y23 (quot (* 2 height) 3)
        scatter 5]
    (when (< (rand) 0.5) ; slow it down a bit
      [[(+ x3 (rand-int scatter)) (rand-int y23)]
       [(- width x3 (rand-int scatter)) (+ (quot y23 2) (rand-int y23))]])))

(defn- create-walls []
  (into (create-rand-walls) (create-vertical-walls)))

(defmethod update-state :generation
  [state]
  (update state :walls into (create-walls)))


(defn- draw-text [walls]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text (str "Walls: " (count walls)) 10 (- scene-height 32))
  (q/text "Press   SPACE   to stop" 10 (- scene-height 10)))

(defmethod draw :generation
  [{:keys [borders walls]}]
  (q/background 200)
  (draw-text walls)
  (q/fill 60)
  (q/scale size)
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))


(defmethod key-press :generation
  [state]
  (case (q/key-as-keyword)
    :space (-> state
               (assoc :screen-type :navigation)
               (assoc :scene-start (q/millis)))
    :q     (-> state
               (assoc :screen-type :intro)
               (assoc :walls #{}))
    state))
