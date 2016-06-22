(defproject thingybot "0.1.0"
  :description "A little Twitter bot"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.385"]
                 [environ "1.0.3"]
                 [clj-http "2.2.0"]
                 [twitter-api "0.7.8" :exclusions [[clj-http]]]]
  :plugins      [[lein-environ "1.0.0"]]
  :main         thingybot.core)
