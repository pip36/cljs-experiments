(ns app.asteroids
  (:require [reagent.core :as r]))

;;CONFIG
(defn color [r g b]
  (str "rgb(" r "," g "," b ")"))

(def black (color 0 0 0))
(def red (color 255 0 0))

;;INPUT HELPERS
(def up-arrow :38)
(def down-arrow :40)
(def left-arrow :37)
(def right-arrow :39)

(def pressed-keys (r/atom {}))

(defn set-pressed-key [keyCode isPressed]
  (swap! pressed-keys assoc (keyword (str keyCode)) isPressed))

(defn key-pressed? [key] (= (key @pressed-keys) true))

(set! (. js/window -onkeydown) 
      (fn [e] (set-pressed-key (. e -keyCode) true)))

(set! (. js/window -onkeyup)
      (fn [e] (set-pressed-key (. e -keyCode) false)))

(defn view []
  (let [view (.getElementById js/document "asteroids")]
   (.getContext view "2d")))

(defn fill-rect [view [x y width height] color]
  (set! (. view -fillStyle) color)
  (.fillRect view x y width height))

(def square-position (r/atom 10))

;; GAME
(def screen-width 500)
(def screen-height 500)

;; SHIP
(def ship-width 10)
(def ship-height 20)
(def ship-speed 10)
(def ship-rotation-speed 15)
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
  (fill-rect (view) [(* -1 (/ ship-width 2)) (* -1 (/ ship-height 2)) ship-width ship-height] black)
  (.restore (view)))


;; GAME LOOP

(defn draw []
  (.clearRect (view) 0 0 screen-width screen-height)
  (draw-ship)
  )

(defn timestamp []
  ((.. js/window -performance -now)))

(def last-render (r/atom (timestamp)))

(defn update-state [delta]
  (update-ship delta)
  (swap! square-position + (* 1 delta)))

(defn frame []
  (let [delta (- (timestamp) @last-render)]
    (update-state (/ delta 60))
    (draw)
    (swap! last-render timestamp)
    (.requestAnimationFrame js/window frame)))

(.requestAnimationFrame js/window frame)

(defn asteroids []
  [:<>
   [:ul
    [:li "Ship Velocity X: "@ship-velocity-x]
    [:li "Ship Velocity Y: "@ship-velocity-y]]
   [:canvas
    {:id "asteroids"
     :width screen-width
     :height screen-height
     :style {:border "1px solid black"}}]])
