(ns parbench.benchmarks
  (:require [com.twinql.clojure.http :as http])
  (:use [lamina core] [aleph http])
  (:import java.util.Calendar))

(def default-http-parameters
  (http/map->params {:handle-redirects false}))

(defn timestamp []
  (.getTimeInMillis (Calendar/getInstance)))

(defn run-request [request]
  "Runs a single HTTP request"
  (let [response (sync-http-request
                    {:method :get :url (:url @request)})
        headers  (:headers response)]
    (dosync
      (alter request assoc
        :responded-at (timestamp)
        :state        :responded
        :status       (:status response))
        :content-length (:content-length headers))))

(defn run-requests [request-list]
  (doseq [request request-list]
    (run-request request)))

(defn user-agents [reqs-state opts]
  "Visualization showing each row as a user agent"
  (let [request-lists (for [row (:grid reqs-state)] (agent row))]
    (doseq [request-list request-lists]
      (send-off request-list run-requests))))