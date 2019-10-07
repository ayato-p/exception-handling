(defproject demo "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring "1.7.1"]]
  :main ^:skip-aot demo.core
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[ring/ring-mock "0.4.0"]]}})
