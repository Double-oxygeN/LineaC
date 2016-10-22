(ns game02.core
  (:require [game02.gui :as gui])
  (:gen-class))

(defn -main
  [& args]
  (gui/game-play 30))
