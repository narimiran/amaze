(ns amaze.intro-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw]]))

(defmethod update-state :intro
  [{:keys [calc-duration scene-start] :as state}]
  (if (>= (calc-duration scene-start) 3)
    (-> state
        (assoc :screen-type :generation)
        (assoc :scene-start (q/millis)))
    state))

(defmethod draw :intro
  [{:keys [calc-duration scene-start]}]
  (q/background 240)
  (q/fill 0)
  (q/text (calc-duration scene-start) 30 140))
