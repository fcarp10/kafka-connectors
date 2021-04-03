# kafka-elasticsearch-connector

Bridge between Kafka and elasticsearch

This is an updated version of the work done by [raymasson](https://github.com/raymasson/kafka-elasticsearch-connector) which seems to not be working anymore. 

All images have been updated to the latest available ones.

## Usage

1. Run the docker-compose file
    ```
    docker-compose up -d
    ```

2. Check elasticsearch and kafka-connector are running in:

    - `http://localhost:8083/` (kafka-connect)
    - `http://localhost:9200/` (elasticsearch)

3. Add connector:

    ```
    curl -X POST -d \
    '{"name":"elasticsearch-sink","config":{"connector.class":"io.confluent.connect.elasticsearch.ElasticsearchSinkConnector","tasks.max":"1","topics":"simple.elasticsearch.data","key.ignore":"true","schema.ignore":"true","connection.url":"http://elasticsearch:9200","type.name":"_doc","name":"elasticsearch-sink"}}' \
    -H "Content-Type: application/json" http://localhost:8083/connectors
    ```

4. Check connector is running in `http://localhost:8083/connectors/elasticsearch-sink/tasks/0/status`.


5. Publish an event to Kafka:

    ```
    go run main.go
    ```
    
    - If you don't have `go` installed:
        ```
        sudo pacman -S go (arch)
        brew install go (macOS)
        ```
    - Other OS, check [here](https://golang.org/doc/install).


6. Check the event is stored in elasticseach `http://localhost:9200/simple.elasticsearch.data/_search?pretty`.