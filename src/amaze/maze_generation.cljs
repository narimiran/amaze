(ns amaze.maze-generation
  (:require
   [quil.core :as q]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [size width height free-pass generating-speed
                         text-size bottom-1 bottom-2 x1 gold-amount
                         background-color]]))


(defn- random-point []
  [(inc (rand-int (- width 2)))
   (inc (rand-int (- height 2)))])

(defn- create-random-walls []
  (let [[x y] (random-point)
        x-    (dec x)
        x+    (inc x)
        y-    (dec y)
        y+    (inc y)
        f     10]
    (condp > (rand)
      (/ 1 f) [[x- y] [x y] [x+ y] [x+ y+]]
      (/ 2 f) [[x- y-] [x y-] [x+ y-] [x- y+] [x+ y+]]
      (/ 3 f) [[x y-] [x- y] [x y] [x+ y] [x y+]]
      (/ 4 f) [[x y-] [x y] [x y+] [x+ y+]]
      (/ 5 f) [[x- y-] [x- y] [x+ y] [x+ y+]]
      (/ 6 f) [[x- y-] [x y-] [x y+] [x+ y+]]
      (/ 7 f) [[x- y+] [x y] [x y-] [x+ y-]]
      (/ 8 f) [[x- y] [x y] [x- y+] [x y+]]
      [[x y]])))

(defn- fade-out [start]
  (- 0.6
     (* 0.00001 (- (q/millis) start))))

(defn- create-elliptical-walls [{:keys [scene-start]}]
  (let [a (quot width 6)
        aa (* 2 a)
        b (quot (* 3 height) 5)
        x (- (rand-int aa) a)
        y (inc (rand-int b))]
    (when (< (+ (/ (* x x) (* a a))
                (/ (* y y) (* b b)))
             1)
    (when (< (rand) (fade-out scene-start))
      [[(+ x aa) y]
       [(- width x aa) (- height y 1)]]))))

(defn- create-walls [state]
  (->> (reduce (fn [acc _]
                 (into acc (create-random-walls)))
               []
               (range generating-speed))
       (into (create-elliptical-walls state))
       (remove free-pass)
       (remove (:borders state))))

(defmethod update-state :generation
  [state]
  (update state :walls into (create-walls state)))


(defn- draw-text [walls]
  (q/fill 0)
  (q/text-size text-size)
  (q/text-style :normal)
  (q/text-align :left)
  (q/text (str "Walls: " (count walls)) x1 bottom-1)
  (q/text "SPACE  stop generating walls" x1 bottom-2))

(defmethod draw :generation
  [{:keys [borders walls scene-start]}]
  (q/background background-color)
  (draw-text walls)
  (q/scale size)
  (q/fill 0)
  (doseq [[x y] borders]
    (q/rect x y 1 1))
  (q/fill (max 0 (- background-color
                    (* 0.04 (- (q/millis) scene-start)))))
  (doseq [[x y] walls]
    (q/rect x y 1 1)))


(defn- place-gold [walls]
  (->> (repeatedly 30 random-point)
       (remove walls)
       (take gold-amount)
       set))

(defmethod key-press :generation
  [{:keys [walls] :as state}]
  (case (q/key-as-keyword)
    (:space :n) (-> state
                    (assoc :screen-type :navigation)
                    (assoc :orig-walls walls)
                    (assoc :gold (place-gold walls))
                    (assoc :scene-start (q/millis)))
    :q          (-> state
                    (assoc :screen-type :intro)
                    (assoc :walls #{}))
    state))
