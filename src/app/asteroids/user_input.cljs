(ns app.asteroids.user-input
  (:require [reagent.core :as r]))

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