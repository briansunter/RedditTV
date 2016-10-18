(ns reddit-tv.reddit.core-test
  (:require [clojure.test :refer :all]
            [reddit-tv.reddit.core :refer [video-ids-from-posts]]
            [reddit-tv.util :refer [check-passed?]]
            [clojure.spec.test :as stest]
            [clojure.spec.gen :as gen]))

(deftest video-id-test
  (is (check-passed? (stest/check `video-ids-from-posts))))
