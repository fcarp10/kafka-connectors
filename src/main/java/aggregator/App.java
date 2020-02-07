package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class App {

   private static final Logger logger = LoggerFactory.getLogger(App.class);
   private static final int CONSUMER_FREQUENCY = 100;

   public static void main(String[] args) throws InterruptedException {

      FirebaseAdmin firebaseAdmin = null;
      KafkaConsumer<String, JsonNode> consumer = null;
      KafkaProducer<String, JsonNode> producer = null;

      // arguments
      if (args.length < 5) {
         logger.error("Missing arguments: [FIREBASE-ADMIN FILE] [FIREBASE_URL] [DATABASE_REF] [KAFKA_BROKER_URL] [TOPICS]");
         System.exit(-1);
      }
      String firebaseAdminFile = args[0];
      String firebaseUrl = args[1];
      String databaseRef = args[2];
      String kafkaBrokerUrl = args[3];
      String topicsString = args[4];
      List<String> topics = Arrays.asList(topicsString.split(","));

      // initialize firebase admin
      try {
         firebaseAdmin = new FirebaseAdmin(firebaseAdminFile, firebaseUrl, databaseRef);
         logger.info("Successfully authenticated to Firebase: " + firebaseUrl);
      } catch (Exception e) {
         logger.error("FIREBASE-ADMIN file and/or FIREBASE_URL are wrong or not accessible");
         System.exit(-1);
      }

      // initialize kafka consumer
      boolean kafkaSubscribed = false;
      do {
         try {
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerUrl);
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "AGGREGATOR");
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topics);
            kafkaSubscribed = true;
         } catch (Exception e) {
            logger.error("KAFKA_BROKER_URL [" + kafkaBrokerUrl + "] is not reachable");
            Thread.sleep(1000);
         }
      } while (!kafkaSubscribed);

      // initialize kafka producer
      try {
         Properties properties = new Properties();
         properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerUrl);
         properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
         properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
         producer = new KafkaProducer<>(properties);
      } catch (Exception e) {
         logger.error("Error while initializing the producer" + e.getMessage());
         System.exit(-1);
      }

      // listening for records
      ObjectMapper mapper = new ObjectMapper();
      logger.info("Listening for records...");
      while (true) {
         ConsumerRecords<String, JsonNode> records = consumer.poll(Duration.ofMillis(CONSUMER_FREQUENCY));
         for (ConsumerRecord<String, JsonNode> record : records) {
            String topic = record.topic();
            JsonNode resourceJsonNode = record.value();
            String id = null;
            Map<String, Object> resource = null;
            // extract the id from the resource
            try {
               resource = mapper.readValue(resourceJsonNode.asText(), Map.class);
               id = resource.get("id").toString();
            } catch (IOException e) {
               logger.error("Error while converting Kafka JSON message to Java object: " + e.getMessage());
            }
            // if the resource has no id, ignore it
            if (id == null) {
               logger.error("JSON object has no id, ignoring...");
            } else {
               logger.info("JSON object received with id: " + id);
               // send resource to firebase
               firebaseAdmin.saveData(topic, id, resource);
               // send resource to producer with modified topic
               ProducerRecord<String, JsonNode> producerRecord = new ProducerRecord<>(topic + "-2", id, resourceJsonNode);
               producer.send(producerRecord);
            }
         }
      }
   }
}
