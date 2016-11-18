(ns kafka-connect-pg-sink.pg
  (:require [clojure.string :as string]
            [postgres.async :as pg]
            [clojure.core.async :refer [<! go]]
            [clojure.tools.logging :as log]))


(defn- list-columns [data]
  (if (map? data)
    (str " (" (string/join ", " (map name (keys data))) ") ")
    (recur (first data))))
(defn- list-params [start end]
  (str "(" (string/join ", " (map (partial str "$")
                                  (range start end))) ")"))
(defn- list-params-seq [data]
  (if (map? data)
    (list-params 1 (inc (count data)))
    (let [size   (count (first data))
          max    (inc (* (count data) size))
          params (map (partial str "$") (range 1 max))]
      (string/join ", " (map
                         #(str "(" (string/join ", " %) ")")
                         (partition size params))))))

(defn create-insert-sql
  "A copy of postgres.async.impl/create-insert-sql that addes WHERE NOT EXISTS"
  [table data on-conflict-cols]
  (let [insert (str "INSERT INTO " table
                    (list-columns data)
                    " VALUES "
                    (list-params-seq data))]
    (if on-conflict-cols
      (str insert
           " ON CONFLICT (" on-conflict-cols ") DO UPDATE SET "
           (list-columns data) " = " (list-params-seq data))
      insert)))

(defn insert*
  "A copy of postgres.async/insert! that uses our custom insert sql"
  [db table data on-conflict-cols]
  (let [insert (create-insert-sql table data on-conflict-cols)
        vs (flatten (map vals data))]
    (log/infof "Postgres insert: %s data: %s" insert (pr-str vs))
    (let [resp (pg/execute! db (list* insert vs))]
      (go (log/infof "Postgres result: %s" (pr-str (<! resp))))
      resp)))
