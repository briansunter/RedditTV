(ns reddit-tv.config
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [environ.core :refer [env]]
   ))

(defn appender-config-with-filename
  [filename]
  {:main (appenders/spit-appender {:fname filename})})

(def appenders
  (let [clj-env (env :clj-env)]
    (case clj-env
      "prod" (appender-config-with-filename "./log/prod.log")
      "dev"  (appender-config-with-filename "./log/dev.log")
      nil)))

(defn setup!
  []
  (doall
   (timbre/merge-config!
    {:level :debug
     :appenders appenders})
   ))
