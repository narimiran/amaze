(ns amaze.config)


(def scene-width 840)
(def scene-height 600)
(def size 12)

(def width (quot scene-width size))
(def height (- (quot scene-height size) 4))

(def start [1 1])
(def finish [(dec (dec width)) (dec height)])

(def generating-speed 2)
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

(def title-size 42)
(def text-size 14)
(def line-height 20)
(def margin 220)

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
(def bomb-limit 5)

(def background-color 210)

(def move-timeout 80)
