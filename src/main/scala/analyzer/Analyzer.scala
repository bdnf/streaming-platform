package analyzer

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import com.mongodb.spark.MongoSpark
import org.bson.Document

object Analyzer {
  def main(args: Array[String]) {

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    //val Array(zkQuorum, group, topics, numThreads) = args
    val sparkConf = new SparkConf()
      .setAppName("KafkaWordCount").setMaster("local[*]")
      .set("spark.mongodb.input.uri", "mongodb://localhost:27017/meetup-rsvps.test_v2")
      .set("spark.mongodb.input.readPreference.name", "secondaryPreferred")
      .set("spark.mongodb.output.uri", "mongodb://127.0.0.1/")
      .set("spark.mongodb.output.database", "meetup-rsvps")
      .set("spark.mongodb.output.collection", "rsvps_collection")

    val ssc = new StreamingContext(sparkConf, Seconds(2))
    //ssc.checkpoint("checkpoint")

    //val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Seq(settings.PUBLISH_TOPIC), kafkaParams)
    )

//    messages.foreachRDD{
//      rdd =>
//        rdd.collect().foreach( r => println(r.value.toString))
//    }

    val entries = messages.map(_.value())

      .foreachRDD(rdd => {
        MongoSpark.save(rdd.map(r => Document.parse(r)))
        println("Saved")
        rdd.collect().foreach(r => {
          if (r.contains("Alexander Mackenzie High School"))
           println(r)



        }
        )
      })




    ssc.start()

    try {
      ssc.awaitTermination()
      println("*** streaming terminated")
    } catch {
      case e: Exception => {
        println("*** streaming exception caught in monitor thread")
      }
    }

    ssc.stop()

  }
}
