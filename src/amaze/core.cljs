(ns amaze.core
  (:require
   [quil.core :as q]
   [quil.middleware :as m]
   [amaze.methods :refer [update-state draw key-press]]
   [amaze.config :refer [scene-width scene-height text-size
                         width height start finish]]
   [amaze.intro-screen]
   [amaze.maze-generation]
   [amaze.navigation]
   [amaze.win-screen]))

(defn- create-borders []
  (-> (mapcat (fn [x] [[x 0] [x (dec height)]]) (range width))
      (into (mapcat (fn [y] [[0 y] [(dec width) y]]) (range height)))
      (->> (remove #{start finish}))
      set))

(defn- calc-scene-duration [scene-start]
  (quot (- (q/millis) scene-start) 1000))

(defn- settings []
  (q/smooth 8))

(defn- setup []
  (q/frame-rate 30)
  (q/no-stroke)
  (q/ellipse-mode :corner)
  (q/text-font "monospace" text-size)
  {:screen-type   :intro
   :cnt           0
   :borders       (create-borders)
   :scene-start   (q/millis)
   :calc-duration calc-scene-duration
   :win-time      0
   :score         0
   :score-shown   0
   :bombs-used    0
   :bomb-expls    []
   :maze-best     0
   :total-best    (or (.. js/window -localStorage (getItem :high-score)) 0)
   :gold          #{}
   :picked-gold   #{}
   :walls         #{}
   :orig-walls    #{}
   :pos           start
   :path          [start]})

(defn -main []
  (q/sketch
   :host "app"
   :size [scene-width scene-height]
   :settings #'settings
   :setup #'setup
   :update #'update-state
   :draw #'draw
   :key-pressed #'key-press
   :middleware [m/fun-mode]))
