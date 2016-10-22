(defproject game02 "0.9.0"
  :description "LineaC : game sample"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot game02.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
