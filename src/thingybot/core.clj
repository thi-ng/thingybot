(ns thingybot.core
  (:require
   [thingybot.config :as config]
   [thingybot.twitter :as twitter]
   [thingybot.modules.lsys.core :as lsys]
   [thingybot.modules.usage.core :as usage]
   [thi.ng.strf.core :as f]
   [environ.core :refer [env]]
   [clojure.core.async :as async :refer [go-loop chan <! close! alts! timeout]]
   [clojure.string :as str]))

(def state (atom {:last-tweet (f/parse-long (env :last-tweet) 10 0)}))

(def module-handlers
  {"lsys" lsys/process-tweet
   "help" usage/process-tweet})

(defn make-job-spec
  [{:keys [id text user] :as tweet}]
  (let [src       (str/trim (str/replace text (str "@" config/bot-name) ""))
        [cmd src] (str/split src #",\s*" 2)]
    {:raw     tweet
     :id      id
     :src     src
     :cmd     cmd
     :params  {:in_reply_to_status_id id}
     :user-id (str "@" (:screen_name user))}))

(defn process-tweet
  [tweet]
  (let [job (make-job-spec tweet)]
    (println "----\nprocessing job:" (:id job) (:user-id job) (:text tweet))
    (some
     (fn [[cmd handler]]
       (when (= cmd (:cmd job))
         (handler job)
         true))
     module-handlers)))

(defn process-mentions
  [{:keys [last-tweet] :as state}]
  (try
    (println "reloading, since:" last-tweet)
    (let [tweets (twitter/get-mentions last-tweet #{config/bot-name})]
      (doseq [tweet tweets]
        (try
          (process-tweet tweet)
          (catch Exception e
            (println "error processing tweet: " (:id tweet) (:text tweet))
            (.printStackTrace e))))
      (assoc state
             :last-tweet (if (seq tweets)
                           (->> tweets (map :id) (sort) (last))
                           last-tweet)))
    (catch Exception e
      (do
        (.printStackTrace e)
        state))))

(defn run
  [state interval]
  (swap! state process-mentions)
  (let [ctrl (chan)]
    (go-loop []
      (let [[_ c] (alts! [ctrl (timeout interval)])]
        (if-not (= c ctrl)
          (do
            (swap! state process-mentions)
            (recur))
          (println "done"))))
    ctrl))

(defn -main
  [& args] (run state 60000))
