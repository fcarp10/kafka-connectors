FROM openjdk:8-jre-alpine
ARG JAR_FILE
WORKDIR /kafka-firebase-aggregator
ADD ${JAR_FILE} app.jar
CMD exec java -jar app.jar firebase-admin.json $FIREBASE_URL $DATABASE_REF $KAFKA_BROKER_URL $TOPICS