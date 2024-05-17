(ns amaze.intro-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw]]))

(defmethod update-state :intro
  [{:keys [calc-duration] :as state}]
  (if (>= (calc-duration state) 3)
    (-> state
        (assoc :screen-type :generation)
        (assoc :scene-start (q/millis)))
    state))

(defmethod draw :intro
  [{:keys [calc-duration] :as state}]
  (q/background 240)
  (q/fill 0)
  (q/text (calc-duration state) 30 140))
