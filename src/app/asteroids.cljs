(ns app.asteroids
  (:require [reagent.core :as r]))

(defn color [r g b]
  (str "rgb(" r "," g "," b ")"))

(def black (color 0 0 0))

(defn view []
  (let [view (.getElementById js/document "asteroids")]
   (.getContext view "2d")))

(defn fill-rect [view [x y width height] color]
  (set! (. view -fillStyle) color)
  (.fillRect view x y width height))

(def square-position (r/atom 10))

(defn draw []
  (.clearRect (view) 0 0 500 500)
  (fill-rect (view) [@square-position 10 50 50] black))

(defn timestamp []
  ((.. js/window -performance -now)))

(def last-render (r/atom (timestamp)))

(defn update-state [delta]
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
