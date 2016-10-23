(ns game02.action
  (:require [game02.game :as gm]))

(defn action-title
  [mode difficulty key counter]
  (reset! difficulty (gm/difficulty-change @key @difficulty))
  (if (= @key :right)
    (dosync
      (ref-set mode :pre-game)
      (ref-set counter 0))))

(defn action-game
  [fps mode key difficulty score player-x counter walls distance]
  (let [diffToInt {:normal 1 :hard 2 :extreme 3}]
    (if (= (rem @counter fps) (dec fps))
      (dosync
        (alter score + (reduce + (gm/wallNumToBinarySeq (first @walls) difficulty)))
        (ref-set walls (gm/next-wall-create @walls difficulty))
        (alter distance (fn [d] (* d 101/100)))))
    (case @key
      :left (swap! player-x (fn [a] (if (> a (- (diffToInt difficulty))) (dec a) a)))
      :right (swap! player-x (fn [a] (if (< a (diffToInt difficulty)) (inc a) a)))
      nil)
    (if (< (* (- @distance 100) (/ fps @distance)) (rem @counter fps) (min (* (- @distance 30) (/ fps @distance)) (dec fps)))
      (if (= (nth (gm/wallNumToBinarySeq (first @walls) difficulty) (+ @player-x (diffToInt difficulty))) 1)
        (dosync
          (ref-set mode :pre-result)
          (ref-set counter 0))))
    (if (> @distance (* fps 50))
      (dosync
        (ref-set mode :pre-success)
        (ref-set counter 0)))))

(defn action-result
  [mode key counter score player-x distance]
  (if (= @key :left)
    (do
      (dosync
        (ref-set mode :title)
        (ref-set counter 0)
        (ref-set score 0)
        (ref-set distance 200))
      (reset! player-x 0))))
