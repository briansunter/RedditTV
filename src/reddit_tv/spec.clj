(ns reddit-tv.spec
  (:require
   [clojure.spec :as s]
   [clojure.spec.gen :as gen]
   [clojure.string :as string]))

(s/def ::client-id string?)
(s/def ::client-secret string?)
(s/def ::client-refresh string?)
(s/def ::youtube-auth (s/keys :req [::client-id ::client-secret ::client-refresh]))

(def youtube-host "youtube.com")

(def youtube-mobile-host "m.youtube.com")
(def youtube-short-host "youtu.be")

(def youtube-short-hosts
  #{youtube-mobile-host youtube-short-host})

(def youtube-hosts
  (conj youtube-short-hosts youtube-host))

(def https "https://")

(s/def ::n (s/with-gen
             pos-int?
             #(s/gen (s/int-in 0 10))))

(defn short-url
  [host path]
  (str https host "/" path))

(defn long-url
  [host query]
  (str "https://" host "?v=" query))

(defn gen-url
  [url-type host-gen]
  (gen/fmap
   (fn [[h q]] (url-type h q))
   (gen/tuple host-gen (gen/string-alphanumeric))))

(defn host
  [h]
  (str h ".com"))

(def gen-host
  (gen/fmap host (gen/such-that #(not= % "")
                                (gen/string-alphanumeric))))

(defn strings-to-path
  [s]
  (clojure.string/join (map #(str "/" %) s)))

(def gen-path
  (gen/fmap strings-to-path (s/gen (s/coll-of (s/nilable string?)))))

(def long-path-url
  (gen/fmap (fn [[h p]] (str "https://" h p))
            (gen/tuple (gen/one-of [gen-host (s/gen youtube-hosts)]) gen-path)))

(s/def ::long-youtube-url (s/with-gen
                            string?
                            #(gen-url long-url (gen/return youtube-host))))

(s/def ::short-youtube-url (s/with-gen
                             string?
                             #(gen-url short-url (s/gen youtube-short-hosts))))

(s/def ::random-url (s/with-gen
                      string?
                      #(gen/one-of [(gen-url short-url gen-host)
                                    (gen-url long-url gen-host)
                                    long-path-url])))

(s/def ::url (s/or :short ::short-youtube-url
                   :long ::long-youtube-url
                   :random ::random-url))

(s/def ::path-frag (s/with-gen string?
                     #(gen/fmap (partial str "/") (s/gen (s/nilable string?)))))

(s/def ::path (s/with-gen string?
                #(gen/fmap string/join (s/gen (s/coll-of ::path-frag)))))

(s/def ::title string?)
(s/def ::score (s/and int?))
(s/def ::created (s/and number? pos?))
(s/def ::data (s/keys :req-un [::title ::score ::created] :opt-un [::url]))
(s/def ::post (s/keys :req-un [::data]))
(s/def ::posts (s/coll-of ::post))
(s/def ::after string?)
(s/def ::post-page (s/keys :req-un [::after ::posts]))
