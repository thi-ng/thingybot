(ns thingybot.modules.lsys.generator
  (:require
   [piksel.core :as pix]
   [thi.ng.math.core :as m]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.utils :as gu]
   [thi.ng.geom.line :as l]
   [thi.ng.geom.rect :as r]
   [thi.ng.geom.vector :as v]
   [thi.ng.color.core :as col])
  (:import
   [java.awt.image BufferedImage]
   [java.awt Color Graphics2D RenderingHints]
   [java.awt.geom Line2D$Double]))

(def default-rules
  {:fwd   [:fwd]
   :left  [:left]
   :right [:right]
   :push  [:push]
   :pop   [:pop]})

(def forward-aliases #{:a :b :c :d :e})

(defn make-agent
  [pos theta length rot-theta]
  {:pos       pos
   :theta     theta
   :rot-theta rot-theta
   :length    length
   :path      []
   :stack     []})

(defn save-agent
  [agent]
  (-> agent
      (update :stack conj (dissoc agent :stack))
      (assoc :path [])))

(defn restore-agent
  [agent]
  {:pre [(pos? (count (:stack agent)))]}
  (let [agent' (peek (:stack agent))]
    (assoc agent'
           :stack (pop (:stack agent))
           :path  (into (:path agent') (:path agent)))))

(defmulti interpret
  (fn [agent sym & _] (if (forward-aliases sym) :fwd sym)))

(defmethod interpret :start
  [agent _]
  agent)

(defmethod interpret :fwd
  [agent _]
  (let [pos  (:pos agent)
        pos' (m/+ pos (g/as-cartesian
                       (v/vec2 (:length agent)
                               (:theta agent))))]
    (-> agent
        (assoc :pos pos')
        (update :path conj (l/line2 pos pos')))))

(defmethod interpret :left
  [agent _]
  (update agent :theta - (:rot-theta agent)))

(defmethod interpret :right
  [agent _]
  (update agent :theta + (:rot-theta agent)))

(defmethod interpret :push
  [agent _]
  (save-agent agent))

(defmethod interpret :pop
  [agent _]
  (restore-agent agent))

(defmethod interpret :default
  [agent sym]
  agent)

(defn interpret-symbols
  [agent syms]
  (reduce interpret agent syms))

(defn rewrite-symbols
  [rules symbols]
  (mapcat (merge default-rules rules) symbols))

(defn eval-system
  [{:keys [width margin rules iter theta rot length]}]
  (let [w' (- width (* 2 margin))
        stream (->> [:start]
                    (iterate (partial rewrite-symbols rules))
                    (take iter)
                    (last))]
    (println "stream length:" (count stream))
    (if (< (count stream) 1e6)
      (->> stream
           (interpret-symbols
            (make-agent (v/vec2) (m/radians theta) length (m/radians rot)))
           (:path)
           (gu/fit-all-into-bounds (r/rect margin margin w' w'))))))

(defn visualize-system
  [{:keys [file width bg]} path]
  (let [path-len        (count path)
        img             (pix/make-image width width)
        ^Graphics2D gfx (.createGraphics img)]
    (doto gfx
      (.setBackground (Color. (int @(col/as-int24 (or bg (col/rgba 1 1 1))))))
      (.clearRect 0 0 width width)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))
    (dorun
     (map-indexed
      (fn [i {[a b] :points}]
        (doto gfx
          (.setColor (Color. (int @(col/as-int24 (col/hsva (/ (double i) path-len) 1.0 1.0)))))
          (.draw (Line2D$Double. (v/x a) (v/y a) (v/x b) (v/y b)))))
      path))
    (pix/save-png file img)))

(defn process-system
  [config]
  (if-let [path (eval-system config)]
    {:result (visualize-system config path)}
    {:error :too-large}))

(comment
  (visualize-system
   {:file   "lsys-gasket.png"
    :width  600
    :margin 10
    :rules  {:start [:fwd :left
                     :fwd :left
                     :fwd :left
                     :fwd :left
                     :fwd]
             :fwd   [:fwd :left
                     :fwd :right :right :fwd :right
                     :fwd :left :fwd :left :fwd]}
    :iter   6
    :theta  0
    :rot    72
    :length 10})

  (visualize-system
   (merge
    config/default-config
    (config/parse-config "#200,6,0,72:s=f-f-f-f-f,f=f-f++f+f-f-f")
    {:file "lsys-gasket.png"}))

  (lsys/visualize-system
   (merge
    config/default-config
    (config/parse-config "#002,9,0,36:s=[b]--[b]--[b]--[b]--[b],a=c--d++++b[+c++++a]--,b=-c++d[+++a++b]-,c=+a--b[---c--d]+,d=++c----a[-d----b]++b")
    {:file "lsys-penrose.png"}))
  )
