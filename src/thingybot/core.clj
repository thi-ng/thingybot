(ns thingybot.core
  (:require
   [twitter.oauth :as oauth]
   [twitter.api.restful :as rest]
   [twitter.request :as req]
   [environ.core :refer [env]]
   [clojure.data.json :as json]
   [clojure.core.async :as async :refer [go-loop chan <! close! alts! timeout]]
   [clojure.string :as str]))

(def creds
  (oauth/make-oauth-creds
   (env :twitter-app-consumer-key)
   (env :twitter-app-consumer-secret)
   (env :twitter-user-access-token)
   (env :twitter-user-access-token-secret)))

(def bot-name (env :bot-name))

(def state
  (atom {:last-tweet 0
         :replies {}}))

(def phrases
  {#"\b(hello|hi|hiya|hey)\b" ["good greetings" "goode day to you"]
   #"\bcan i\b" ["i do beseech you"]
   #"\byou\b" "thou"
   #"\byour\b" ["thy" "ye"]
   #"\bhave\b" "hast"
   #"\bhas\b" "hath"
   #"\bare\b" "art"
   #"\bcan't" "cannot"
   #"\bdo it" "do't"
   #"\bdon't" "do not"
   #"\bisn't" "is not"
   #"\bit\b" "'t"
   #"\b(it's|it is|this is)\b" "'tis"
   #"\bnot\b" "nay"
   #"\b(should|must)\b" "shalt"
   #"\b(shouldn't|mustn't)\b" "shan't"
   #"\buntil\b" "'til"
   #"\bright\b" "true"
   #"\bbye\b" ["fare thee well" "be blessed"]
   #"\bsoon\b" ["before long"]
   })

(defn send-tweet
  [msg & [opts]]
  (println "tweeting: " msg)
  (rest/statuses-update :oauth-creds creds
                        :params (merge {:status msg} opts)))

(defn apply-past-tense
  [w]
  (if-let [w' (re-find #"(.*)ed(.?)$" w)]
    (str (second w') "'d" (last w'))
    w))

(defn replace-phrases
  [replacements msg]
  (reduce
   (fn [acc [m r]] (str/replace acc m (if (string? r) r (rand-nth r))))
   msg replacements))

(defn replace-user
  [user msg]
  (str/replace msg (str "@" bot-name) user))

(defn process-tweet
  [{:keys [text user] :as tweet}]
  (println "----\n" tweet)
  (->> text
       (str/lower-case)
       (replace-user (str "@" (:screen_name user)))
       (replace-phrases phrases)
       (re-seq #"[\w@!\?,\.'\"]+")
       (map apply-past-tense)
       (str/join " ")))

(defn process-mentions
  [{:keys [last-tweet replies] :as state}]
  (try
    (println "reloading, since:" last-tweet)
    (let [replies' (->> (rest/statuses-mentions-timeline
                         :oauth-creds creds
                         :params (if (pos? last-tweet) {:since-id (:last-tweet state)}))
                        :body
                        (into
                         {}
                         (comp
                          (filter #(not= bot-name (get-in % [:user :screen_name])))
                          (map #(vector (:id %) {:src (:text %) :reply (process-tweet %)})))))
          replies  (merge replies replies')]
      (println "new replies:" replies')
      (doseq [[id r] replies']
        (send-tweet (:reply r) {:in_reply_to_status_id id}))
      (assoc state
             :replies replies
             :last-tweet (if (seq replies) (->> replies keys (sort) (last)) last-tweet)))
    (catch Exception e
      (do
        (.printStackTrace e)
        state))))

(defn run
  [state interval]
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

(comment
  [{:favorite_count 0,
    :entities {:hashtags [], :symbols [], :user_mentions [{:screen_name "thingybot", :name "thingybot", :id 3093638524, :id_str "3093638524", :indices [0 10]}], :urls []},
    :text "@thingybot hi",
    :retweet_count 0,
    :coordinates nil,
    :in_reply_to_status_id_str nil,
    :contributors nil,
    :in_reply_to_user_id_str "3093638524",
    :id_str "577991730923966464",
    :in_reply_to_screen_name "thingybot",
    :retweeted false,
    :truncated false,
    :created_at "Wed Mar 18 00:35:41 +0000 2015",
    :lang "und",
    :geo nil,
    :place nil,
    :in_reply_to_status_id nil,
    :user {:profile_use_background_image true, :follow_request_sent false, :entities {:url {:urls [{:url "http://t.co/A6ftZTBLpV", :expanded_url "http://postspectacular.com", :display_url "postspectacular.com", :indices [0 22]}]}, :description {:urls []}}, :default_profile false, :profile_sidebar_fill_color "F2F2E8", :protected false, :following false, :profile_background_image_url "http://pbs.twimg.com/profile_background_images/485768169869430786/de3HbNLR.jpeg", :default_profile_image false, :contributors_enabled false, :favourites_count 617, :time_zone "London", :profile_location nil, :name "Karsten Schmidt", :id_str "5135", :listed_count 598, :utc_offset 0, :profile_link_color "FFD630", :profile_background_tile false, :location "London", :statuses_count 1489, :followers_count 7989, :friends_count 716, :profile_banner_url "https://pbs.twimg.com/profile_banners/5135/1404651259", :created_at "Fri Sep 01 08:37:04 +0000 2006", :lang "en", :profile_sidebar_border_color "FFFFFF", :url "http://t.co/A6ftZTBLpV", :notifications false, :profile_background_color "02084D", :geo_enabled false, :is_translation_enabled false, :profile_image_url_https "https://pbs.twimg.com/profile_images/259831681/toxi_128x128_normal.jpg", :is_translator false, :profile_image_url "http://pbs.twimg.com/profile_images/259831681/toxi_128x128_normal.jpg", :verified false, :id 5135, :profile_background_image_url_https "https://pbs.twimg.com/profile_background_images/485768169869430786/de3HbNLR.jpeg", :description "computational design, data, interaction, code, opensource, art, education, research, MA (Hons) of Nothing", :profile_text_color "474040", :screen_name "toxi"},
    :favorited false,
    :source "<a href=\"https://about.twitter.com/products/tweetdeck\" rel=\"nofollow\">TweetDeck</a>",
    :id 577991730923966464,
    :in_reply_to_user_id 3093638524}])

(comment
  (rest/statuses-update-with-media
   :oauth-creds creds
   :body        [(req/file-body-part "shakespeare.jpg")
                 (req/status-body-part "to be or not to be...")]
   :params      {:in_reply_to_status_id 578049844847087617})

  )
