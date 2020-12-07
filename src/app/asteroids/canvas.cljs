(ns app.asteroids.canvas)

(defn color [r g b]
  (str "rgb(" r "," g "," b ")"))

(def black (color 0 0 0))
(def red (color 255 0 0))

(defn view []
  (let [view (.getElementById js/document "asteroids")]
    (.getContext view "2d")))

(defn fill-rect [view [x y width height] color]
  (set! (. view -fillStyle) color)
  (.fillRect view x y width height))