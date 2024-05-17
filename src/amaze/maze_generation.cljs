(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw]]
   [amaze.config :refer [size width height free-pass generating-speed]]))

(defn- create-walls []
  (->> (repeatedly generating-speed
                   (fn [_] [(rand-int width) (rand-int height)]))
       (remove free-pass)))

(defmethod update-state :generation
  [{:keys [calc-duration scene-start] :as state}]
  (if (>= (calc-duration scene-start) 5)
    (-> state
        (assoc :screen-type :navigation)
        (assoc :scene-start (q/millis)))
    (-> state
        (update :walls into (create-walls)))))

(defmethod draw :generation
  [{:keys [borders walls]}]
  (q/background 200)
  (q/fill 40)
  (q/scale size)
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))
