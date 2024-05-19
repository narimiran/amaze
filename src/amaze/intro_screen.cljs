(ns amaze.intro-screen
  (:require
   [quil.core :as q]
   [amaze.methods :refer [draw key-press]]
   [amaze.config :refer [title-size scene-width scene-height text-size]]))


(defn- draw-title []
  (q/text-size title-size)
  (q/text-align :center)
  (q/text-style :bold)
  (q/text "aMAZE" (/ scene-width 2) (/ scene-height 3)))

(defn- draw-text []
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :center)
  (q/text "Press   SPACE   to play"
          (/ scene-width 2) (* 2 (/ scene-height 3))))

(defmethod draw :intro
  [_state]
  (q/background 10)
  (q/fill 250)
  (draw-title)
  (draw-text))


(defmethod key-press :intro
  [state]
  (case (q/key-as-keyword)
    :space (-> state
               (assoc :screen-type :generation)
               (assoc :scene-start (q/millis)))
    state))
