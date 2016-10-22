(ns game02.game
  (:require [clojure.pprint :refer [cl-format]]))

(def key-convert {37 :left 38 :up 39 :right 40 :down})

(defn difficulty-change
  [key current-diff]
  (case [key current-diff]
    [:up :normal] :hard
    [:up :hard] :extreme
    [:down :hard] :normal
    [:down :extreme] :hard
    current-diff))

(def wall-max {:normal 3 :hard 5 :extreme 7})

(defn next-wall-create
  [current-wall difficulty]
  (conj (vec (rest current-wall)) (rand-int (dec (bit-shift-left 1 (wall-max difficulty))))))

(defn wallNumToBinarySeq
  [wall-num difficulty]
  (vec (map (fn [^Character n] (Character/digit n 10)) (cl-format nil (str "~" (wall-max difficulty) ",'0',B") wall-num))))
