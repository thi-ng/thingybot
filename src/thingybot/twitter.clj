(ns thingybot.twitter
  (:require
   [thingybot.config :as config]
   [twitter.api.restful :as rest]
   [twitter.request :as req]
   [environ.core :refer [env]]
   [clojure.string :as str]))

(defn send-tweet
  [msg & [params]]
  (println "sending tweet: " msg)
  (try
    (rest/statuses-update
     :oauth-creds config/creds
     :params      (merge {:status msg} params))
    true
    (catch Exception e
      false)))

(defn send-media-tweet
  [img-path msg params]
  (println "sending media tweet: " img-path msg)
  (try
    (rest/statuses-update-with-media
     :oauth-creds config/creds
     :body        [(req/file-body-part img-path)
                   (req/status-body-part msg)]
     :params      params)
    true
    (catch Exception e
      false)))

(defn get-mentions
  [since exclusions]
  (->> (rest/statuses-mentions-timeline
        :oauth-creds config/creds
        :params      (if (pos? since) {:since-id since}))
       (:body)
       (filter #(not (exclusions (get-in % [:user :screen_name]))))))
