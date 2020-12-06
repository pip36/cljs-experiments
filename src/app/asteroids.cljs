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

;; SHIP
(def ship-width 10)
(def ship-height 20)
(def ship-speed 10)
(def ship-rotation-speed 10)
(def ship-rotation-degrees (r/atom 0))
(def ship-position-x (r/atom 100))
(def ship-position-y (r/atom 100))
(def ship-directions {left-arrow -1
                      right-arrow 1})

(defn to-radians [degrees]
  (/ (* degrees (.. js/Math -PI)) 180))

(defn update-ship [delta]
  (let [left (if (key-pressed? left-arrow) -1 0)
        right (if (key-pressed? right-arrow) 1 0)
        up (if (key-pressed? up-arrow) 1 0)]
    (swap! ship-rotation-degrees + (* (+ left right) ship-rotation-speed delta))
    (swap! ship-position-x + (* (.sin js/Math (to-radians @ship-rotation-degrees)) up ship-speed delta))
    (swap! ship-position-y + (* (.cos js/Math (to-radians @ship-rotation-degrees)) -1 up ship-speed delta))
    ))
  
(defn draw-ship []
  (.save (view))
  (.translate (view) @ship-position-x @ship-position-y)
  (.rotate (view) (to-radians @ship-rotation-degrees))
  (fill-rect (view) [(* -1 (/ ship-width 2)) (* -1 (/ ship-height 2)) ship-width ship-height] black)
  (.restore (view)))


;; GAME LOOP

(defn draw []
  (.clearRect (view) 0 0 500 500)
  (draw-ship)
  (fill-rect (view) [@square-position 10 50 50] black))

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
  [:canvas
   {:id "asteroids"
    :width "500"
    :height "500"
    :style {:border "1px solid black"}}])
