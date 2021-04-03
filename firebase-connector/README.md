# kafka-firebase-connector

Bridge between Kafka and Firebase realtime database

## Usage

1. In docker-compose file, modify the EXTERNAL IP address to expose kafka broker
    ```
    KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:9093,EXTERNAL://localhost:9092
    ```

2. Put Firebase config file in the same foler or modify path: 
    ```
    - ./firebase-admin.json:/kafka-firebase-aggregator/firebase-admin.json
    ```

3. Specify Firebase database URL:
    ```
    - FIREBASE_URL=https://database-name.firebaseio.com
    ```

4. Specify a name for the database reference:`
    ```
    - DATABASE_REF=root
    ```

5. Specify topics to subscribe:
    ```
    - TOPICS=test1,test2
    ```

6. Run the docker-compose file
    ```
    docker-compose up -d
    ```
