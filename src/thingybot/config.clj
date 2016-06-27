(ns thingybot.config
  (:require
   [twitter.oauth :as oauth]
   [environ.core :refer [env]]))

(def creds
  (oauth/make-oauth-creds
   (env :twitter-app-consumer-key)
   (env :twitter-app-consumer-secret)
   (env :twitter-user-access-token)
   (env :twitter-user-access-token-secret)))

(def bot-name (env :bot-name))


