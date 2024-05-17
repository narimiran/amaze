(ns amaze.navigation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw]]
   [amaze.config :refer [size]]))

(defmethod update-state :navigation
  [{:keys [calc-duration] :as state}]
  (if (>= (calc-duration state) 4)
    (-> state
        (assoc :screen-type :intro)
        (assoc :walls #{})
        (assoc :scene-start (q/millis)))
    state))

(defmethod draw :navigation
  [{:keys [calc-duration borders walls]
    [x-pos y-pos] :pos
    :as state}]
  (q/background 200)
  (q/fill 40)
  (q/text (str "Elapsed " (calc-duration state)) 30 80)
  (q/scale size)
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (doseq [[x y] walls]
    (q/rect x y 1 1))
  (q/fill 80 200 170)
  (q/ellipse x-pos y-pos 1 1))
