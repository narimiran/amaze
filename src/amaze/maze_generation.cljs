(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size width height free-pass generating-speed
                         text-size scene-height]]))

(defn- create-walls []
  (->> (repeatedly generating-speed
                   (fn [_] [(rand-int width) (rand-int height)]))
       (remove free-pass)))

(defmethod update-state :generation
  [state]
  (update state :walls into (create-walls)))


(defn- draw-text []
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text "Press   SPACE   to stop" 10 (- scene-height 20)))

(defmethod draw :generation
  [{:keys [borders walls]}]
  (q/background 200)
  (draw-text)
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
    state))
