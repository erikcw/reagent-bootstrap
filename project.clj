(defproject reagent-bootstrap "0.1.0-SNAPSHOT"
  :description "Simple hiccup style wrappers for twitter bootstrap"
  :url "https://github.com/steeni/reagent-bootstrap"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [reagent "0.4.2"]
                 [im.chit/purnam.test "0.4.3"]
                 [prismatic/dommy "0.1.3"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "reagent-bootstrap"
              :source-paths ["src"]
              :compiler {
                :output-to "reagent-bootstrap.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}
             {:id "tests"
              :source-paths ["src" "test"]
              :compiler {:output-to "harness/unit/test-reagent-bootstrap.js"}}]})
