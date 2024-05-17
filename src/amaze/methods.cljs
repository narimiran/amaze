(ns amaze.methods)


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
