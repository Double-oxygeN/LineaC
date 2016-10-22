(ns game02.gui
  (:import (java.awt Color Dimension Font Graphics Toolkit)
           (javax.swing JFrame JPanel Timer)
           (java.awt.event ActionListener KeyListener))
  (:require [clojure.java.io :as io]
            [game02.game :as gm]))

(defn- abs [n]
  (max (- n) n))

(def image01
  (-> (Toolkit/getDefaultToolkit)
    (.getImage (io/resource "logo.png"))))

(defn- action-title
  [mode difficulty key counter]
  (reset! difficulty (gm/difficulty-change @key @difficulty))
  (if (= @key :right)
    (dosync
      (ref-set mode :pre-game)
      (ref-set counter 0))))

(defn- action-game
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

(defn- action-result
  [mode key counter score player-x distance]
  (if (= @key :left)
    (do
      (dosync
        (ref-set mode :title)
        (ref-set counter 0)
        (ref-set score 0)
        (ref-set distance 200))
      (reset! player-x 0))))

(defn- paint-title
  [g difficulty observer]
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

(defn- paint-pre-game
  [g fps w h c difficulty observer]
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

(defn- drawWall
  [g wall y difficulty]
  (let [bs (gm/wallNumToBinarySeq wall difficulty) start-x {:normal 195 :hard 125 :extreme 55}]
    (doseq [i (range (gm/wall-max difficulty))]
      (if-not (zero? (nth bs i)) (.fillRect g (+ (* 70 i) (start-x difficulty)) y 70 5)))))

(defn- paint-game
  [g fps difficulty score player-x walls counter distance observer]
  (.setColor g Color/yellow)
  (doseq [line-num (range 4)]
    (drawWall g (nth walls line-num) (+ (* -4 distance) (* (- 3 line-num) distance) (int (* (rem counter fps) distance (/ 1 fps))) 800) difficulty))
  (doto g
    (.drawImage image01 (+ 265 (* player-x 70)) 700 70 70 observer)
    (.setColor (Color. 0.0 0.3 1.0 0.7))
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString (str score) 260 220)))

(defn- paint-pre-result
  [g fps w h difficulty score player-x walls counter distance observer]
  (let [alpha (float (/ counter (* fps 3.3)))]
    (.setColor g Color/yellow)
    (doseq [line-num (range 4)]
      (drawWall g (nth walls line-num) (+ (* -4 distance) (* (- 3 line-num) distance) (int (* distance 2/3)) 800) difficulty))
    (doto g
      (.drawImage image01 (+ 265 (* player-x 70)) 700 70 70 observer)
      (.setColor (Color. 0.0 0.3 1.0 0.7))
      (.setFont (Font. "Menlo" Font/PLAIN 42))
      (.drawString (str score) 260 100)
      (.setColor (Color. 1.0 0.0 0.0 0.4))
      (.fillRect 0 0 w h)
      (.setColor (Color. 0.0 0.0 0.0 alpha))
      (.fillRect 0 0 w h))))

(defn- paint-result
  [g score]
  (doto g
    (.setColor Color/green)
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString "RESULT" 180 200)
    (.drawString (str score " walls") 120 280)
    (.drawString "GAME OVER" 150 400)
    (.drawString "<< TITLE" 130 650)))

(defn- paint-pre-success
  [g fps w h difficulty score player-x counter observer]
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

(defn- paint-success
  [g score]
  (doto g
    (.setColor Color/green)
    (.setFont (Font. "Menlo" Font/PLAIN 42))
    (.drawString "RESULT" 180 200)
    (.drawString (str score " walls") 120 280)
    (.drawString "<< TITLE" 130 650)
    (.setColor Color/yellow)
    (.drawString "COMPLETED!" 150 400)))

(defn game-panel
  "GUIを管理する関数"
  [fps]
  (let [panel-width 600
        panel-height 800
        mode (ref :title)
        counter (ref 0)
        difficulty (atom :normal)
        score (ref 0)
        player-x (atom 0)
        key (atom :none)
        walls (ref [0 0 0 0])
        distanceB2Wall (ref 200)]
    (proxy [JPanel ActionListener KeyListener] []
      (paintComponent [g]
        (doto g
          (.setColor Color/black)
          (.fillRect 0 0 panel-width panel-height))
        (case @mode
          :title (paint-title g @difficulty this)
          :pre-game (paint-pre-game g fps panel-width panel-height @counter @difficulty this)
          :game (paint-game g fps @difficulty @score @player-x @walls @counter @distanceB2Wall this)
          :pre-result (paint-pre-result g fps panel-width panel-height @difficulty @score @player-x @walls @counter @distanceB2Wall this)
          :result (paint-result g @score)
          :pre-success (paint-pre-success g fps panel-width panel-height @difficulty @score @player-x @counter this)
          :success (paint-success g @score)
          nil))
      (actionPerformed [e]
        (case @mode
          :title (action-title mode difficulty key counter)
          :pre-game (if (> @counter (* fps 2)) (dosync (ref-set mode :game) (ref-set counter 0)))
          :game (action-game fps mode key @difficulty score player-x counter walls distanceB2Wall)
          :pre-result (if (> @counter (* fps 3.2)) (dosync (ref-set mode :result) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :result (action-result mode key counter score player-x distanceB2Wall)
          :pre-success (if (> @counter (* fps 4)) (dosync (ref-set mode :success) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :success (action-result mode key counter score player-x distanceB2Wall)
          nil)
        (.repaint this)
        (dosync (alter counter inc))
        (reset! key :none))
      (keyPressed [e]
        (-> (.getKeyCode e)
          (gm/key-convert)
          ((fn [kc] (reset! key kc)))))
      (keyTyped [e])
      (keyReleased [e]
        (reset! key :none))
      (getPreferredSize []
        (Dimension. panel-width panel-height)))))

(defn game-play
  "fpsを引数にとって，ゲームを開始する関数"
  [fps]
  (let [frame (JFrame. "LineaC")
        panel (game-panel fps)
        timer (Timer. (int (/ 1000 fps)) panel)]
    (doto panel
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame
      (.setDefaultCloseOperation javax.swing.WindowConstants/EXIT_ON_CLOSE)
      (.setResizable false)
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start timer)))
