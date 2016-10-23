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
        distance (ref 200)
        counter (ref 0)
        key (atom :none)]
    (proxy [JPanel ActionListener KeyListener] []
      (paintComponent [g]
        (let [paint-of {:title gui/paint-title
                        :pre-game gui/paint-pre-game
                        :game gui/paint-game
                        :pre-result gui/paint-pre-result
                        :result gui/paint-result
                        :pre-success gui/paint-pre-success
                        :success gui/paint-success}]
          (doto g
            (.setColor Color/black)
            (.fillRect 0 0 panel-width panel-height))
          ((paint-of @mode) g fps panel-width panel-height @difficulty @score @player-x @walls @distance @counter this)))
      (actionPerformed [e]
        (case @mode
          :title (ac/action-title mode difficulty key counter)
          :pre-game (if (> @counter (* fps 2)) (dosync (ref-set mode :game) (ref-set counter 0)))
          :game (ac/action-game fps mode key @difficulty score player-x counter walls distance)
          :pre-result (if (> @counter (* fps 3.2)) (dosync (ref-set mode :result) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :result (ac/action-result mode key counter score player-x distance)
          :pre-success (if (> @counter (* fps 4)) (dosync (ref-set mode :success) (ref-set counter 0) (ref-set walls [0 0 0 0])))
          :success (ac/action-result mode key counter score player-x distance)
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
  (game-play 60))
