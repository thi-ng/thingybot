(ns thingybot.modules.lsys.core
  (:require
   [thingybot.modules.lsys.generator :as gen]
   [thingybot.twitter :as twitter]
   [thi.ng.color.core :as col]
   [thi.ng.strf.core :as f]
   [clojure.string :as str]))

(def default-config
  {:width  1280
   :margin 20
   :length 10
   :theta  0
   :rot    90})

(def valid-symbols
  #{:start :fwd :left :right :push :pop :a :b :c :d :e :x :y :z})

(defn replace-symbols
  [src]
  (let [src (replace
             {\s :start
              \f :fwd
              \+ :right \- :left
              \[ :push \] :pop
              \a :a \b :b \c :c \d :d \e :e
              \x :x \y :y \z :z}
             src)
        num (count src)]
    (loop [acc [] i 0]
      (if (< i num)
        (let [sym (nth src i)]
          (if (#{\2 \3 \4 \5 \6 \7 \8 \9} sym)
            (recur (into acc (repeat (- (int sym) 48) (nth src (inc i))))
                   (+ i 2))
            (recur (conj acc sym) (inc i))))
        acc))))

(defn translate-rule
  [[_ _ id expr]]
  [(keyword (first (replace-symbols id)))
   (vec (replace-symbols expr))])

(defn parse-system-spec
  [src]
  (let [src                 (str/lower-case src)
        [hd rules]          (str/split src #"\s*:\s*")
        [bg iter theta rot] (str/split hd #"\s*,\s*")
        rules               (if rules
                              (->> rules
                                   (re-seq #"(([a-z])\s*=\s*([a-z2-9\-\+\[\]]+))")
                                   (map translate-rule)
                                   (into {})))]
    {:bg    (try (col/as-int24 (col/css bg)) (catch Exception e))
     :iter  (f/parse-int iter 10 16)
     :theta (f/parse-int theta 10 0)
     :rot   (f/parse-int rot 10 90)
     :rules rules}))

(defn validate-rules
  [rules]
  (and (:start rules)
       (->> rules
            (mapcat val)
            (every? valid-symbols))))

(defn process-tweet
  [{:keys [id src params user-id]}]
  (println "module lsys:" src)
  (let [img-path (str id ".png")
        config   (parse-system-spec src)]
    (if (validate-rules (:rules config))
      (let [res (gen/process-system
                 (merge default-config config {:file img-path}))
            msg (if (> (count src) 110) (str (subs src 0 110) "...") src)]
        (cond
          (:result res)
          (twitter/send-media-tweet
           img-path (str user-id " " msg) params)

          (= :too-large (:error res))
          (do (twitter/send-tweet
               (str user-id
                    " the system expands to over 1 million steps,"
                    " please reduce iteration count")
               params)
              false)
          :else
          (do (twitter/send-tweet
               (str user-id " I seem to have some problem saving images")
               params)
              false)))
      (do (twitter/send-tweet
           (str user-id
                " your instructions contained an error,"
                " please try again! kthxbai")
           params)
          false))))
