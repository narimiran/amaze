(ns amaze.methods
  (:require [quil.core :as q]))


(defmulti update-state :screen-type)
(defmethod update-state :default
  [state]
  state)

(defmulti draw :screen-type)
(defmethod draw :default
  [_state])

(defmulti key-press :screen-type)
(defmethod key-press :default
  [state]
  state)


(defn change-screen [state new-screen-type & [opts]]
  (-> (reduce-kv (fn [state k v]
                   (assoc state k v))
                 state
                 opts)
      (assoc :screen-type new-screen-type)
      (assoc :scene-start (q/millis))))
