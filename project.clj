(defproject thingybot "0.1.0"
  :description "A little Twitter bot"
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [environ "1.0.0"]
                 [twitter-api "0.7.8"]]
  :plugins      [[lein-environ "1.0.0"]]
  :main         thingybot.core)
