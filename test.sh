#!/usr/bin/env bash -xe
ls target/*pg-sink-standalone.jar || lein uberjar
docker-compose up -d

DOCKER=$1

until curl $DOCKER:8083
do
    sleep 1
done



(cat ~/.pgpass | grep "$DOCKER:5432:postgres:postgres:password") || \
    echo "$DOCKER:5432:postgres:postgres:password" >> ~/.pgpass
chmod 0600 ~/.pgpass

psql --host $1 --port 5432 --user postgres -c 'create table if not exists id_test (id1 VARCHAR(20), field1 VARCHAR(4), field2 VARCHAR(4), guid VARCHAR(36), tertiary_id VARCHAR(10), PRIMARY KEY (id1, field1, field2));'

echo '{"id":"1234567890123456789","fields":["foo","bar"],"guid":"12345678-90ab-cdef-1234-567890abcdef","someother_id":"12345678"}' \
    | kafkacat -P -t test-topic -b $1:9092

curl $DOCKER:8083/connectors/pg-sink-test -XDELETE || echo "old connector removed"
curl $1:8083/connectors -X POST -H'Content-type: application/json' -H'Accept: application/json' -d'{
  "name": "pg-sink-test",
  "config": {
    "topics": "test-topic",
    "connector.class": "org.clojars.yanatan16.kafka.connect.pg.PostgresSinkConnector",
    "tasks.max": 1,
    "db.hostname": "postgres",
    "db.database": "postgres",
    "db.username": "postgres",
    "db.password": "password",
    "db.table": "id_test",
    "tuple.spec.json": "{\"id1\":[\"value\",\"id\"],\"field1\":[\"value\",\"fields\",0],\"field2\":[\"value\",\"fields\",1],\"guid\":[\"value\",\"guid\"],\"tertiary_id\":[\"value\",\"someother_id\"]}"
  }
}'

sleep 30
psql --host docker --port 5432 --user postgres -c 'select * from id_test'
