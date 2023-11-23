(defproject de.dixieflatline/lehmer "0.1.0-SNAPSHOT"
  :description "Encode permutations to numbers and vice versa."
  :url "https://github.com/20centaurifux/lehmer"
  :license {:name "AGPLv3"
            :url "https://www.gnu.org/licenses/agpl-3.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/test.check "1.1.1"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :test {:dependencies [[org.clojure/core.match "1.0.1"]]}}
  :plugins [[lein-marginalia "0.9.1"]])
