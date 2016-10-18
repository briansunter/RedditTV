(ns reddit-tv.core
  (:require
   [clojure.spec :as s]
   [clojure.core.async :refer [go-loop >! <!! chan] :as a]
   [environ.core :refer [env]]
   [taoensso.timbre :refer [debug]]
   [clojure.spec.test :refer [instrument]]
   [reddit-tv.config :refer [setup!]]
   [reddit-tv.reddit.api :refer [get-posts-page]]
   [reddit-tv.spec :as rs]
   [reddit-tv.youtube.api :refer [get-youtube-auth-token
                                  create-youtube-playlist
                                  add-video-to-playlist]]
   [reddit-tv.youtube.core :refer [playlist-title]]
   [reddit-tv.reddit.core :refer [video-id-xforms]]
   ))

(defn get-posts
  [ch]
  (go-loop [a nil]
    (let [res   (get-posts-page a)
          posts (:children res)
          after (:after res)]
      (doseq [p posts]
        (>! ch p))
      (recur after))))

(defn upload-to-youtube
  [ch playlist-name]
  (let [token (get-youtube-auth-token)
        playlist-id (create-youtube-playlist playlist-name token)]
    (loop []
      (when-let [video-id (<!! ch)]
        (add-video-to-playlist video-id playlist-id token)
      (recur)))))

(s/fdef get-n-posts
        :args (s/cat :n ::rs/n)
        :ret ::rs/children)

(defn get-n-posts
  [n]
  (let [ch (chan)]
    (get-posts ch)
    (<!! (a/into [] (a/take n ch)))))

(defn -main
  []
  (instrument)
  (debug "starting " (env :clj-env))
  (let [posts-chan (chan 3 video-id-xforms)]
    (debug "starting reddit consumer")
    (get-posts posts-chan)
    (debug "starting youtube uploader")
    (upload-to-youtube (a/take 15 posts-chan) playlist-title)))
