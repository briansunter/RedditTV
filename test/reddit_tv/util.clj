(ns reddit-tv.util)

(defn check-passed?
  [c]
  (not-any? #(-> % :failure) c))
