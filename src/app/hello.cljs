(ns app.hello
  (:require [reagent.core :as r]
            [app.todo-list :refer [todo-list]]
            [app.asteroids :refer [asteroids]]))

(def click-count (r/atom 0))

(defn handle-click [fn]
  (if (< (fn @click-count) 0)
    (js/alert "Can't go below 0!")
    (swap! click-count fn)))

(defn log [] (js/alert "hi"))

(defn click-counter [click-count]
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:button {:on-click #(handle-click inc)} "Up"]
   [:button {:on-click #(handle-click dec)} "Down"]])

(defn hello []
  [:<>
   [:p "Hello, cljs-first-app is running!"]
   [:p "Here's an example of using a component with state:"]
   [click-counter click-count]
   [todo-list]
   [asteroids]])
