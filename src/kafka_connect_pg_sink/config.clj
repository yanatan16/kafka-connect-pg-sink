(ns kafka-connect-pg-sink.config
  (:require [franzy.connect.config :refer [make-config-def] :as cfg]
            [cheshire.core :as json]))


(defn json-tuple-spec-validator []
  (cfg/validator
   (fn [val]
     (try (let [spec (json/parse-string val true)]
            (and (map? spec)
                 (not-empty spec)
                 (every? (fn [[col path]]
                           (and (vector? path)
                                (or (empty? path) (#{"value" "key"} (first path)))
                                (every? #(or (string? %) (integer? %)) path)))
                         spec)))
          (catch Exception e false)))
   "Invalid JSON Tuple Specification"))

(defn config []
  (make-config-def
   (:db.hostname :type/string ::cfg/no-default-value :importance/high
                 "PostgreSQL Instance Hostname")
   (:db.port :type/int (int 5432) :importance/high
             "PostgreSQL Instance Port")
   (:db.database :type/string "postgres" :importance/high
                 "PostgreSQL Database Name")
   (:db.username :type/string "postgres" :importance/high
                 "PostgreSQL Instance Login Username")
   (:db.password :type/string "" :importance/high
                 "PostgreSQL Instance Login Password")
   (:db.table :type/string ::cfg/no-default-value :importance/high
                 "PostgreSQL Table Name")

   (:insert.on.conflict.columns :type/string ::cfg/no-default-value :importance/medium
                                "If set, insertion will use: ON CONFLICT (columns) DO UPDATE SET (cols)")

   (:tuple.spec.json :type/string ::cfg/no-default-value
                     (json-tuple-spec-validator) :importance/high
                     "Tuple generation specification in JSON. Should be a map of column names to extraction paths. Each extraction path is an array of strings and numbers that specifies a nested value to extract as that tuple-column value. Example: {\"id\": [\"key\",\"id\"], \"foo\": [\"value\",\"foo\"]}")
   ))
