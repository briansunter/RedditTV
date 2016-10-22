(ns reddit-tv.youtube.core
  (:require
   [clj-time.core :as t]
   [clj-time.coerce :as c]
   [clj-time.format :as f]))

(def youtube-formatter (f/formatter "EEEE MMMM d, yyyy"))
(def formatted-today (f/unparse youtube-formatter (t/now)))
(def playlist-title (str "Top Reddit videos of " formatted-today))
