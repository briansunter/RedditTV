(defproject reddit-tv "0.1.0"
  :description "A bot that takes the top videos from Reddit
and turns them into a YouTube playlist."
  :url "http://briansunter.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.6.3"]
                 [clj-http "2.2.0"]
                 [clj-time "0.12.0"]
                 [com.cemerick/url "0.1.1"]
                 [com.taoensso/timbre "4.7.4"]
                 [environ "1.1.0"]
                 [org.clojure/clojure "1.9.0-alpha11"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/test.check "0.9.0"]
                 [robert/bruce "0.8.0"]
                 [slingshot "0.12.2"]]
  :dev {:dependencies [[org.clojure/test.check "0.9.0"]]}
  :main ^:skip-aot reddit-tv.core
  :plugins [[lein-environ "1.1.0"] [lein-cloverage "1.0.8"]]
  :target-path "target/%s"
  :monkeypatch-clojure-test false
  :profiles {:uberjar {:aot :all}
             :repl {:env {:clj-env "repl"}}
             :dev {:env {:clj-env "dev"}}
             :test {:env {:clj-env "test"}}
             :prod  {:env {:clj-env "prod"}}
             }
  :aliases {"launch" ["trampoline" "with-profile" "+prod" "run"]})
