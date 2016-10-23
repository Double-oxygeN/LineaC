(ns game02.core
  (:import (java.awt Color Dimension)
           (javax.swing JFrame JPanel Timer)
           (java.awt.event ActionListener KeyListener))
  (:require [game02.gui :as gui]
            [game02.game :as gm]
            [game02.action :as ac])
  (:gen-class))

(defn game-panel
  "GUIを管理する関数"
  [fps]
  (let [panel-width 600
        panel-height 800
        mode (ref :title)
        difficulty (atom :normal)
        score (ref 0)
        player-x (atom 0)
        walls (ref [0 0 0 0])
        distanceB2Wall (ref 200)
        counter (ref 0)
        key (atom :none)]
    (proxy [JPanel ActionListener KeyListener] []
      (paintComponent [g]
        (doto g
          (.setColor Color/black)
          (.fillRect 0 0 panel-width panel-height))
        (case @mode
          :title (gui/paint-title g @difficulty this)
          :pre-game (gui/paint-pre-game g fps panel-width panel-height @counter @difficulty this)
          :game (gui/paint-game g fps @difficulty @score @player-x @walls @counter @distanceB2Wall this)
          :pre-result (gui/paint-pre-result g fps panel-width panel-height @difficulty @score @player-x @walls @counter @distanceB2Wall this)
          :result (gui/paint-result g @score)
          :pre-success (gui/paint-pre-success g fps panel-width panel-height @difficulty @score @player-x @counter this)
          :success (gui/paint-success g @score)
          nil))
      (actionPerformed [e]
        (case @mode
          :title (ac/action-title mode difficulty key counter)
          :pre-game (if (> @counter (* fps 2)) (dosync (ref-set mode :game) (ref-set counter 0)))
          :game (ac/action-game fps mode key @difficulty score player-x counter walls distanceB2Wall)
          :pre-result (if (> @counter (* fps 3.2)) (dosync (ref-set mode :result) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :result (ac/action-result mode key counter score player-x distanceB2Wall)
          :pre-success (if (> @counter (* fps 4)) (dosync (ref-set mode :success) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :success (ac/action-result mode key counter score player-x distanceB2Wall)
          nil)
        (.repaint this)
        (dosync (alter counter inc))
        (reset! key :none))
      (keyPressed [e]
        (-> (.getKeyCode e)
          (#(get gm/key-convert % :none))
          (#(reset! key %))))
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

(defn -main
  [& args]
  (game-play 30))
