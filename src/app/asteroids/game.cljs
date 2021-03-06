(ns app.asteroids.game
  (:require [reagent.core :as r]
            [app.asteroids.user-input 
             :refer [key-pressed? left-arrow right-arrow up-arrow]]
            [app.asteroids.canvas 
             :refer [view fill-rect black red]]))

;; GAME
(def screen-width 500)
(def screen-height 500)

;; SHIP
(def ship-width 10)
(def ship-height 20)
(def ship-speed 40)
(def ship-rotation-speed 20)
(def ship-rotation-degrees (r/atom 0))
(def ship-velocity-x (r/atom 0))
(def ship-velocity-y (r/atom 0))
(def ship-position-x (r/atom (/ screen-width 2)))
(def ship-position-y (r/atom (/ screen-height 2)))
(def ship-directions {left-arrow -1
                      right-arrow 1})

(defn to-radians [degrees]
  (/ (* degrees (.. js/Math -PI)) 180))

(defn clamp-wrap [val min max]
  (cond
   (< val min) max
   (> val max) min
   :else val))

(defn clamp [val min max]
  (cond
    (< val min) min
    (> val max) max
    :else val))

(defn update-ship [delta]
  (let [left (if (key-pressed? left-arrow) -1 0)
        right (if (key-pressed? right-arrow) 1 0)
        up (if (key-pressed? up-arrow) 1 0)]
    (swap! ship-rotation-degrees + (* (+ left right) ship-rotation-speed delta))
    (swap! ship-velocity-x + (* (.sin js/Math (to-radians @ship-rotation-degrees)) up (/ ship-speed 20) delta))
    (swap! ship-velocity-y + (* (.cos js/Math (to-radians @ship-rotation-degrees)) -1 up (/ ship-speed 20) delta))
    (swap! ship-position-x + (* @ship-velocity-x delta))
    (swap! ship-position-y + (* @ship-velocity-y delta))
    (swap! ship-position-x 
           (fn [v] (clamp-wrap v (- 0 ship-width) (+ screen-width ship-width))))
    (swap! ship-position-y 
           (fn [v] (clamp-wrap v (- 0 ship-width) (+ screen-height ship-width))))
    ))
  
(defn draw-ship []
  (.save (view))
  (.translate (view) @ship-position-x @ship-position-y)
  (.rotate (view) (to-radians @ship-rotation-degrees))
  (fill-rect (view) [(* -1 (/ ship-width 2)) (* -1 (/ ship-height 2)) ship-width ship-height] red)
  (.restore (view)))

;; ASTEROIDS
(defn hit-ship? [asteroid]
  (let [a-top-left [(- (:position-x asteroid) (/ (:size asteroid) 2))
                    (- (:position-y asteroid) (/ (:size asteroid) 2))]
        a-bottom-right [(+ (:position-x asteroid) (/ (:size asteroid) 2))
                        (+ (:position-y asteroid) (/ (:size asteroid) 2))]
        s-top-left [(- @ship-position-x (/ ship-width 2))
                    (- @ship-position-y (/ ship-height 2))]
        s-bottom-right [(+ @ship-position-x (/ ship-width 2))
                        (+ @ship-position-y (/ ship-height 2))]]
    (not (or (> (a-top-left 0) (s-bottom-right 0))
             (< (a-bottom-right 0) (s-top-left 0))
             (> (a-top-left 1) (s-bottom-right 1))
             (< (a-bottom-right 1) (s-top-left 1))))))

(def max-asteroid-size 100)
(def min-asteroid-size 50)
(def max-asteroid-velocity 10) 
(def max-asteroid-rotation-speed 0.1)
(defn random-asteroid []
  {:id (symbol (str (rand-int 1000000000)))
   :size (clamp (rand-int max-asteroid-size) 
                min-asteroid-size max-asteroid-size)
   :position-x (rand-int screen-width)
   :position-y (rand-int screen-height)
   :velocity-x (- max-asteroid-velocity (rand-int (* 2 max-asteroid-velocity)))
   :velocity-y (- max-asteroid-velocity (rand-int (* 2 max-asteroid-velocity)))
   :rotation-degrees 0
   :rotation-speed (- max-asteroid-rotation-speed (rand (* 2 max-asteroid-rotation-speed)))})

(def asteroids (r/atom {:1 (random-asteroid)
                        :2 (random-asteroid)
                        :3 (random-asteroid)
                        :4 (random-asteroid)
                        :5 (random-asteroid)}))

(defn update-asteroids [delta]
  (doseq [[id asteroid] @asteroids]
    (swap! asteroids update-in [id :rotation-degrees] 
           (fn [rot] (+ rot (* (:rotation-speed asteroid delta)))))
    (swap! asteroids update-in [id :position-x] 
           (fn [x] (+ x (* (:velocity-x asteroid) delta))))
    (swap! asteroids update-in [id :position-y] 
           (fn [x] (+ x (* (:velocity-y asteroid) delta))))
    (swap! asteroids update-in [id :position-x]
           (fn [v] (clamp-wrap v (- 0 (:size asteroid)) (+ screen-width (:size asteroid)))))
    (swap! asteroids update-in [id :position-y]
           (fn [v] (clamp-wrap v (- 0 (:size asteroid)) (+ screen-height (:size asteroid)))))))
 
(defn draw-asteroids []
  (doseq [[id asteroid] @asteroids] 
    (.save (view))
    (.translate (view) (:position-x asteroid) (:position-y asteroid))
    (.rotate (view) (to-radians (:rotation-degrees asteroid)))
    (fill-rect (view) [(* -1 (/ (:size asteroid) 2)) (* -1 (/ (:size asteroid) 2)) (:size asteroid) (:size asteroid)] black)
    (.restore (view))))


;; GAME LOOP

(defn draw []
  (.clearRect (view) 0 0 screen-width screen-height)
  (draw-ship)
  (draw-asteroids)
  )

(defn timestamp []
  ((.. js/window -performance -now)))

(def last-render (r/atom (timestamp)))

(defn update-state [delta]
  (update-ship delta)
  (update-asteroids delta)
  )

(defn frame []
  (let [delta (- (timestamp) @last-render)]
    (update-state (/ delta 60))
    (draw)
    (swap! last-render timestamp)
    (.requestAnimationFrame js/window frame)))

(.requestAnimationFrame js/window frame)

(defn asteroids-game []
  [:<>
   [:ul
    [:li "Ship Velocity X: "@ship-velocity-x]
    [:li "Ship Velocity Y: "@ship-velocity-y]
    [:li "Hit Asteroid 1 : " (when (hit-ship? (:1 @asteroids)) "YES")]
    [:li "Hit Asteroid 2 : " (when (hit-ship? (:2 @asteroids)) "YES")]]
   [:canvas
    {:id "asteroids"
     :width screen-width
     :height screen-height
     :style {:border "1px solid black"}}]])
