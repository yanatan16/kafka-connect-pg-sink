(ns kafka-connect-pg-sink.sink
  (:require [franzy.connect.sink :as sink]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [postgres.async :as pg]
            [kafka-connect-pg-sink.pg :refer [insert*]]
            [clojure.core.async :refer [<!!]]
            [kafka-connect-pg-sink.config :refer [config]])
  (:import [java.io InputStream ByteArrayInputStream]))

(defn get-tuple [tuple-spec record]
  (reduce-kv
   (fn [m col path] (assoc m col (get-in record path)))
   {}
   tuple-spec))

(defn put-records [{:keys [table db tuple-spec] :as state} records]
  (log/debugf "Pushing %d kafka record to PostgreSQL. first: %s state: %s" (count records) (pr-str (first records)) (pr-str state))
  (when (not-empty records)
    (->> records
         (map #(get-tuple tuple-spec %))
         (insert* db table)

         ;;TODO: Make this a deferred action that flushes
         <!!))
  state)

(defn parse-json-tuple-spec [json]
  (->> (json/parse-string json true)
       (reduce-kv (fn [m k path] (assoc m k (map #(if (string? %) (keyword %) %) path))) {})))

(defn start [cfg]
  (log/infof "Starting PostgreSQL Sink Task with config: %s" (pr-str cfg))
  (let [tuple-spec (parse-json-tuple-spec (:tuple.spec.json cfg))]
    {:tuple-spec tuple-spec
     :table (:db.table cfg)
     :db (pg/open-db {:hostname (:db.hostname cfg)
                      :port (:db.port cfg)
                      :database (:db.database cfg)
                      :username (:db.username cfg)
                      :password (:db.password cfg)})}))

(defn stop [{db :db}]
  (log/info "Stopping PostgreSQL Sink Task.")
  (pg/close-db! db))

(sink/make-sink
 org.clojars.yanatan16.kafka.connect.pg.PostgresSink
 {:start start
  :stop stop
  :put put-records}

 {:config-def config
  :start (fn [cfg _]
           (log/infof "Starting PostgreSQL Sink Connector with config: %s" (pr-str cfg))
           cfg)
  :stop (fn [_] (log/info "Stopping PostgreSQL Sink Connector"))})
