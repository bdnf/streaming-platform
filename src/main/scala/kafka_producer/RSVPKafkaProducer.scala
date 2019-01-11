package kafka_producer

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}
import org.apache.kafka.common.serialization.{StringSerializer, StringDeserializer}

class RSVPKafkaProducer {
  case class KafkaProducerConfigs(brokerList: String = "127.0.0.1:9092") {
    val properties = new Properties()
    properties.put("bootstrap.servers", brokerList)
    properties.put("key.serializer", classOf[StringSerializer])
    properties.put("value.serializer", classOf[StringSerializer])
    //    properties.put("serializer.class", classOf[StringDeserializer])
    //    properties.put("batch.size", 16384)
    //    properties.put("linger.ms", 1)
    //    properties.put("buffer.memory", 33554432)
  }

  val producer = new KafkaProducer[String, String](KafkaProducerConfigs().properties)

  def produce(topic: String, message: String): Unit =
    producer.send(new ProducerRecord[String, String](topic, message))

  def close() = producer.close(100L, TimeUnit.MILLISECONDS)
}

