(ns amaze.config)

(def scene-width 800)
(def scene-height 600)
(def size 10)

(def width (/ scene-width size))
(def height (- (/ scene-height size) 5))

(def start [1 0])
(def finish [(dec (dec width)) (dec height)])

(def generating-speed 4)
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
(def margin 300)
