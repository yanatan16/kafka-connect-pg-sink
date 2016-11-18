(defproject kafka-connect-pg-sink "0.1.5"
  :description "Kafka Connector Sink to push records to postgres"
  :url "https://github.com/yanatan16/kafka-connect-pg-sink"
  :license {:name "MIT"
            :url "https://github.com/yanatan16/kafka-connect-pg-sink/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojars.yanatan16/franzy-connect "0.1.1"]
                 [alaisi/postgres.async "0.8.0"]
                 [cheshire "5.6.3"]]
  :aot :all

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :uberjar-name "kafka-connect-pg-sink-standalone.jar")
