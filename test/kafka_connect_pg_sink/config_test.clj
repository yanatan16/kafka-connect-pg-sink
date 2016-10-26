(ns kafka-connect-pg-sink.config-test
  (:require [clojure.test :refer [deftest is]]
            [kafka-connect-pg-sink.config :refer :all])
  (:import [org.apache.kafka.common.config ConfigException]))

(deftest json-tuple-spec-validator-test
  (let [validate #(.ensureValid (json-tuple-spec-validator) "foo" %)]
    (is (nil? (validate "{\"col\": []}")))
    (is (nil? (validate "{\"col\": [\"key\"], \"col2\": [\"value\"]}")))
    (is (nil? (validate "{\"col\": [\"value\", \"field\", 0]}")))

    (is (thrown? ConfigException (validate "")))
    (is (thrown? ConfigException (validate "{}")))
    (is (thrown? ConfigException (validate "[\"value\",\"field\"]")))
    (is (thrown? ConfigException (validate "{\"col\": [\"value\" {\"foo\": \"bar\"}]}")))
    (is (thrown? ConfigException (validate "\"foo\"")))
    (is (thrown? ConfigException (validate "badjson")))))
