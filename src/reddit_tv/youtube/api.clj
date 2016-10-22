(ns reddit-tv.youtube.api
  (:require
   [cemerick.url :refer [url]]
   [slingshot.slingshot :refer [try+]]
   [clj-http.client :as http]
   [environ.core :refer [env]]
   [taoensso.timbre :refer [log debug warn error]]
   [clojure.core.async :refer [<!!]]
   [cheshire.core :refer [parse-string generate-string]]
   [clojure.spec :as s]
   [reddit-tv.spec :as rs]))

(defn url-builder
  [base & paths]
  (url (clojure.string/join "/" (cons base paths))))

(def ^:private youtube-auth-url-base (url "https://www.googleapis.com/oauth2/v3/token"))
(def ^:private youtube-base-url "https://www.googleapis.com/youtube/v3")
(def ^:private youtube-playlist-path "playlists")
(def ^:private youtube-playlist-url (url-builder youtube-base-url youtube-playlist-path))
(def ^:private youtube-playlist-item-path "playlistItems")
(def ^:private youtube-playlist-item-url (url-builder youtube-base-url youtube-playlist-item-path))

(def ^:private youtube-auth-keys
  {:client_id (env :youtube-client-id)
   :refresh_token (env :youtube-refresh-token)
   :client_secret (env :youtube-client-secret)
   :grant_type "refresh_token"})

(def ^:private youtube-auth-url
  (update youtube-auth-url-base :query #(merge youtube-auth-keys %)))

(s/fdef get-youtube-auth-token
        :args nil?
        :ret string?)

(defn get-youtube-auth-token
  []
  (debug "getting youtube auth token")
  (-> youtube-auth-url
      str
      http/post
      :body
      (parse-string true)
      :access_token))

(defn- youtube-auth-headers
  [token]
  {:authorization (str "Bearer " token)
   :content-type  "application/json"})

(defn- youtube-playlist-body
  [name]
  {:snippet {:title name}
   :status {:privacyStatus "public"}})

(defn- run-youtube-request
  [request]
  (try+
   (-> (http/request request)
       :body
       (parse-string true)
       :id)
   (catch [:status 404] _
     (warn "youtube video was deleted. skipping..."))
   (catch [:status 403] _
     (warn "youtube wouldn't let us add that one. skipping..."))))

(defn- create-playlist-request
  [name token]
  (println "creating youtube playlist " name)
  {:url (str youtube-playlist-url)
   :query-params {:part "snippet,status"}
   :method :post
   :body (generate-string (youtube-playlist-body name))
   :headers (youtube-auth-headers token)})

(s/fdef create-youtube-playlist
        :args (s/cat :name string? :token string?)
        :ret string?)

(defn create-youtube-playlist
  [name token]
  (debug "creating youtube playlist")
  (run-youtube-request (create-playlist-request name token)))

(defn- youtube-playlist-item-body
  [video-id playlist-id]
  {:snippet {:playlistId playlist-id
             :resourceId {:videoId video-id
                          :kind "youtube#video"}}})

(defn- add-video-to-playlist-request
  [video-id playlist-id token]
  {:url (str youtube-playlist-item-url)
   :query-params {:part "snippet"}
   :method :post
   :body (generate-string (youtube-playlist-item-body video-id playlist-id))
   :headers (youtube-auth-headers token)})

(s/fdef add-video-to-playlist
        :args (s/cat :video-id string? :playlist-id string? :token string?)
        :ret string?)

(defn add-video-to-playlist
  [video-id playlist-id token]
  (debug "adding video id " video-id " to playlist " playlist-id)
  (run-youtube-request (add-video-to-playlist-request video-id playlist-id token)))
