(ns amaze.config)


(def scene-width 900)
(def scene-height 750)
(def size 15)

(def width (quot scene-width size))
(def height (- (quot scene-height size) 3))

(def start [1 0])
(def finish [(dec (dec width)) (dec height)])

(def generating-speed 8)
(def free-area 3)

(def free-pass
  (let [[sx sy] start
        [fx fy] finish]
    (->> (for [dx (range free-area)
               dy (range free-area)]
           [[(+ sx dx) (+ sy dy)]
            [(- fx dx) (- fy dy)]])
         (apply concat)
         set)))

(def title-size 48)
(def text-size 16)
(def line-height 20)
(def margin 250)

(def x1 size)
(def x2 (* 0.22 scene-width))
(def x3 (* 0.44 scene-width))
(def x4 (* 0.60 scene-width))
(def x5 (* 0.78 scene-width))
(def bottom-1 (- scene-height 28))
(def bottom-2 (- scene-height 8))

(def gold-amount 2)
(def gold-multi 50)
(def bomb-multi 20)
(def bomb-limit 5)

(def background-color 180)
