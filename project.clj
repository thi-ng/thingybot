(defproject thingybot "0.1.0-SNAPSHOT"
  :description  "modular twitter bot"
  :url          "http://thi.ng/thingybot"
  :license      {:name "Apache Software Licence"
                 :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.385"]
                 [thi.ng/geom "0.0.1173-SNAPSHOT"]
                 [twitter-api "0.7.8" :exclusions [[clj-http]]]
                 [clj-http "2.2.0"]
                 [environ "1.0.3"]]
  :plugins      [[lein-environ "1.0.0"]]
  :main         thingybot.core)
