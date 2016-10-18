(ns reddit-tv.reddit.core
  (:require
   [reddit-tv.spec :as rs]
   [taoensso.timbre :refer [log info]]
   [clojure.string :as string]
   [clojure.spec :as s]
   [cemerick.url :refer [url]]))

(defn video-id-from-path
  [p]
  (let [path-frags (string/split p #"/")]
    (case path-frags
      [] nil
      [""] nil
      (nth path-frags 1))))

(defmulti video-id-from-url :host)

(defmethod video-id-from-url rs/youtube-host [u]
  (get-in u [:query :v]))

(s/fdef video-id-from-path
        :args (s/cat :path ::rs/path)
        :ret string?)

(defmethod video-id-from-url rs/youtube-short-host  [u]
  (video-id-from-path (:path u)))

(defmethod video-id-from-url rs/youtube-mobile-host [u]
  (video-id-from-path (:path u)))

(def filter-youtube
  (filter #(rs/youtube-hosts (:host %))))

(def video-id-xforms
  (comp
   (map :data)
   (map :url)
   (remove nil?)
   (map url)
   filter-youtube
   (map video-id-from-url)
   (remove string/blank?)
   ))

(s/fdef video-ids-from-posts
        :args (s/cat :posts (s/coll-of ::rs/post))
        :ret (s/or :none empty? :ids (s/coll-of string?)))

(defn video-ids-from-posts
  [posts]
  (into [] video-id-xforms posts))
