## Real-time streaming platform using Akka, Kafka, Spark

(Note: work in progress)

This repo shows how to build and deploy real-time streaming platform. Made as a part of Akka/Kafka/Spark learning journey. 

#### Services:

Akka Client consumes event from external API and publishes them in Kafka topic.

Kafka stores event messages.

Spark Streaming Service consumes events from topic, Analyzes them and stores in Long-term storage (here used MongoDB).



