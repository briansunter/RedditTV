(ns reddit-tv.reddit.core
  (:require
   [reddit-tv.spec :as rs]
   [taoensso.timbre :refer [log info]]
   [clojure.string :as string]
   [clojure.spec :as s]
   [cemerick.url :refer [url]]))

(defn video-id-from-long-url
  [u]
  (second (re-find #"youtube\.com.*v=([a-zA-Z0-9]+)" u)))

(defn video-id-from-short-url
  [u]
  (second (re-find #"youtu\.be/([a-zA-Z0-9]+)" u)))

(defn video-id-from-url
  [u]
  (or (video-id-from-long-url u) (video-id-from-short-url u)))

(def video-id-xforms
  (comp
   (map :data)
   (map :url)
   (remove nil?)
   (map video-id-from-url)
   (remove nil?)))

(s/fdef video-ids-from-posts
        :args (s/cat :posts (s/coll-of ::rs/post))
        :ret (s/or :none empty? :ids (s/coll-of string?)))

(defn video-ids-from-posts
  [posts]
  (into [] video-id-xforms posts))
