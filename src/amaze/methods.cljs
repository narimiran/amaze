(ns amaze.methods)

(defmulti update-state :screen-type)
(defmethod update-state :default
  [state]
  state)

(defmulti draw :screen-type)
(defmethod draw :default
  [_state])
