(ns game02.gui
  (:import (java.awt Color Font Graphics Toolkit))
  (:require [clojure.java.io :as io]
            [game02.game :as gm]))

(defn- abs [n]
  (max (- n) n))

(def image01
  (-> (Toolkit/getDefaultToolkit)
    (.getImage (io/resource "logo.png"))))

(defn paint-title
  [g _ _ _ difficulty _ _ _ _ _ observer]
  (doto g
    (.drawImage image01 250 100 100 100 observer)
    (.setColor Color/green)
    (.setFont (Font. "Menlo" Font/PLAIN 64))
    (.drawString "LineaC" 180 250)
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString (name difficulty) 210 500)
    (.drawString "START >>" 200 650))
  (if-not (= difficulty :extreme)
    (.fillPolygon g (int-array [270 330 300]) (int-array [450 450 420]) 3))
  (if-not (= difficulty :normal)
    (.fillPolygon g (int-array [270 330 300]) (int-array [520 520 550]) 3)))

(defn paint-pre-game
  [g fps w h difficulty _ _ _ _ c observer]
  (let [alpha (- 1.0 (float (/ (abs (- c fps)) (inc fps))))]
    (if (< c fps)
      (doto g
        (.drawImage image01 250 100 100 100 observer)
        (.setColor Color/green)
        (.setFont (Font. "Menlo" Font/PLAIN 64))
        (.drawString "LineaC" 180 250)
        (.setFont (Font. "Menlo" Font/PLAIN 42))
        (.drawString (name difficulty) 210 500)
        (.setColor (Color. 1.0 0.5 0.0))
        (.drawString "START >>" 200 650))
      (doto g
        (.drawImage image01 265 700 70 70 observer)
        (.setColor (Color. 0.0 0.3 1.0 0.7))
        (.setFont (Font. "Menlo" Font/PLAIN 42))
        (.drawString "0" 260 220)))
    (doto g
      (.setColor (Color. 1.0 1.0 1.0 alpha))
      (.fillRect 0 0 w h))))

(defn drawWall
  [g wall y difficulty]
  (let [bs (gm/wallNumToBinarySeq wall difficulty) start-x {:normal 195 :hard 125 :extreme 55}]
    (doseq [i (range (gm/wall-max difficulty))]
      (if-not (zero? (nth bs i)) (.fillRect g (+ (* 70 i) (start-x difficulty)) y 70 5)))))

(defn paint-game
  [g fps _ _ difficulty score player-x walls distance counter observer]
  (.setColor g Color/yellow)
  (doseq [line-num (range 4)]
    (drawWall g (nth walls line-num) (+ (* -4 distance) (* (- 3 line-num) distance) (int (* (rem counter fps) distance (/ 1 fps))) 800) difficulty))
  (doto g
    (.drawImage image01 (+ 265 (* player-x 70)) 700 70 70 observer)
    (.setColor (Color. 0.0 0.3 1.0 0.7))
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString (str score) 260 220)))

(defn paint-pre-result
  [g fps w h difficulty score player-x walls distance counter observer]
  (let [alpha (float (/ counter (* fps 3.3)))]
    (.setColor g Color/yellow)
    (doseq [line-num (range 4)]
      (drawWall g (nth walls line-num) (+ (* -4 distance) (* (- 3 line-num) distance) (int (- distance 100)) 800) difficulty))
    (doto g
      (.drawImage image01 (+ 265 (* player-x 70)) 700 70 70 observer)
      (.setColor (Color. 0.0 0.3 1.0 0.7))
      (.setFont (Font. "Menlo" Font/PLAIN 42))
      (.drawString (str score) 260 100)
      (.setColor (Color. 1.0 0.0 0.0 0.4))
      (.fillRect 0 0 w h)
      (.setColor (Color. 0.0 0.0 0.0 alpha))
      (.fillRect 0 0 w h))))

(defn paint-result
  [g _ _ _ _ score _ _ _ _ _]
  (doto g
    (.setColor Color/green)
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString "RESULT" 180 200)
    (.drawString (str score " walls") 120 280)
    (.drawString "GAME OVER" 150 400)
    (.drawString "<< TITLE" 130 650)))

(defn paint-pre-success
  [g fps w h difficulty score player-x _ _ counter observer]
  (let [alpha (float (/ counter (* fps 4.2)))]
    (doto g
      (.drawImage image01 (+ 265 (* player-x 70)) (- 700 (* counter 10)) 70 70 observer)
      (.setColor (Color. 0.0 0.3 1.0 0.7))
      (.setFont (Font. "Menlo" Font/PLAIN 42))
      (.drawString (str score) 260 100)
      (.setColor (Color. 0.3 1.0 0.0 0.4))
      (.fillRect 0 0 w h)
      (.setColor (Color. 0.0 0.0 0.0 alpha))
      (.fillRect 0 0 w h))))

(defn paint-success
  [g _ _ _ _ score _ _ _ _ _]
  (doto g
    (.setColor Color/green)
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString "RESULT" 180 200)
    (.drawString (str score " walls") 120 280)
    (.drawString "<< TITLE" 130 650)
    (.setColor Color/yellow)
    (.drawString "COMPLETED!" 150 400)))
