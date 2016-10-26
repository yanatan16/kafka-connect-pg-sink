# kafka-connect-pg-sink

A kafka sink connector for pushing records to PostgreSQL.

There is [another postgres connector](https://github.com/justonedb/kafka-sink-pg-json) out there, but it doesn't work with system-level key and value conversion.

## Usage

To install into a kafka-connect classpath, simply download an uberjar from the releases page or build it yourself:

```
lein uberjar
cp target/kafka-connect-pg-sink-standalone.jar <destination classpath>
```

Then you can start connectors as normal through the REST API or Confluent's Kafka Control Center.

## Configuration

The `connector.class` is `org.clojars.yanatan16.kafka.connect.pg.PostgresSinkConnector`.

It has the following custom configurations (above and beyond the [normal sink configurations](http://docs.confluent.io/2.0.0/connect/userguide.html#configuring-connectors)).

- `tuple.spec.json` Tuple generation specification in JSON. Should be a map of column names to extraction paths. Each extraction path is an array of strings and numbers that specifies a nested value to extract as that tuple-column value. Example: `{\"id\": [\"key\",\"id\"], \"foo\": [\"value\",\"foo\"]}`
- `db.hostname` Instance Hostname
- `db.port` Instance Port (defaults to 5432)
- `db.database` Database name
- `db.username` Username
- `db.password` Password
- `db.table` Table name (can have schema prefix)

## Testing

Unit tests can be run in the normal clojure way:

```
lein test
```

Integration tests can be run with:

```
$ ./test.sh <DOCKER_HOSTNAME>
...<stuff>...
         id1         | field1 | field2 |                 guid                 | tertiary_id
---------------------+--------+--------+--------------------------------------+-------------
 1234567890123456789 | foo    | bar    | 12345678-90ab-cdef-1234-567890abcdef | 12345678
(1 row)
```

If you see `(0 rows)`, then the test failed and you should check out the logs in `docker-compose logs connect`.

## License

See [LICENSE](/LICENSE) file.
