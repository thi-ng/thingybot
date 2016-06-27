(ns thingybot.modules.usage.core
  (:require
   [thingybot.twitter :as twitter]))

(defn process-tweet
  [{:keys [user-id params]}]
  (twitter/send-tweet
   (str user-id " for now visit http://thi.ng/thingybot for usage instructions")
   params))
