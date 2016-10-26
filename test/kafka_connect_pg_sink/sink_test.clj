(ns kafka-connect-pg-sink.sink-test
  (:require [clojure.test :refer [deftest is]]
            [kafka-connect-pg-sink.sink :refer :all]))

(deftest get-tuple-test
  (is (= (get-tuple {:col1 [:key] :col2 [:value :data]}
                    {:key "foo" :value {:data "bar"}})
         {:col1 "foo" :col2 "bar"}))
  (is (= (get-tuple {:col1 [:key] :col2 [:value :data]}
                    {:key {:id "foo"} :value {:data ["bar" "baz"]}})
         {:col1 {:id "foo"} :col2 ["bar" "baz"]})))

(deftest parse-json-tuple-spec-test
  (is (= (parse-json-tuple-spec "{}") {}))
  (is (= (parse-json-tuple-spec "{\"col\": []}") {:col []}))
  (is (= (parse-json-tuple-spec "{\"col\": [\"key\"], \"col2\": [\"value\"]}") {:col [:key] :col2 [:value]}))
  (is (= (parse-json-tuple-spec "{\"col\": [\"value\", \"field\", 0]}") {:col [:value :field 0]})))

(deftest sink-available
  (is (instance? org.apache.kafka.connect.sink.SinkConnector
                 (new org.clojars.yanatan16.kafka.connect.pg.PostgresSinkConnector)))
  (is (instance? org.apache.kafka.connect.sink.SinkTask
                 (new org.clojars.yanatan16.kafka.connect.pg.PostgresSinkTask))))
