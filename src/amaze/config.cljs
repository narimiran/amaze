(ns amaze.config)


(def scene-width 880)
(def scene-height 704)
(def size 16)

(def width (quot scene-width size))
(def height (- (quot scene-height size) 3))

(def start [1 1])
(def finish [(dec (dec width)) (dec height)])

(def free-area 3)

(def title-size 36)
(def text-size 14)
(def line-height 20)
(def left-margin 480)
(def right-margin 20)

(def x1 size)
(def x2 (* 0.23 scene-width))
(def x3 (* 0.40 scene-width))
(def x4 (* 0.62 scene-width))
(def x5 (* 0.83 scene-width))
(def bottom-1 (- scene-height 28))
(def bottom-2 (- scene-height 8))

(def gold-amount 2)
(def gold-multi 50)
(def bomb-multi 30)
(def bomb-limit 3)
(def time-multi 3)

(def background-color 210)

(def hard-visibility-limit 10)
