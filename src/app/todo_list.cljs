(ns app.todo-list
  (:require [reagent.core :as r]))

(def todos (r/atom []))
(def input-text (r/atom ""))

(defn add-todo [todo]
  (swap! todos #(conj @todos todo)))

(defn set-input-text [text]
  (swap! input-text (fn [] text)))

(defn enter? [keycode] (= keycode 13))



(defn todo [text isDone]
  [:li text (when isDone " ✔️")])

(defn todo-list []
  ()
  [:<>
   [:input {:on-key-down #(when 
                           (enter? (.. % -keyCode)) 
                            (add-todo {:text @input-text :is-done false}))
            :on-change #(set-input-text (.. % -target -value))
            :value @input-text}]
   [:ul
    (map (fn [x]
           [todo (:text x) (:is-done x)]) @todos)]])