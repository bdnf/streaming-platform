package socket_connection

import akka.{Done}
import akka.actor.{ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import kafka_producer.RSVPKafkaProducer

import scala.concurrent.{Future, Promise}

object SocketClientToKafkaTopic extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  val producer = new RSVPKafkaProducer

  val incoming: Sink[Message, Future[Done]] =
    Flow[Message].mapAsync(settings.NUMBER_OF_CORES) {
      case message: TextMessage.Strict =>
        println("Got response: " +message.text)
        producer.produce(settings.PUBLISH_TOPIC, message.text)
        Future.successful(Done)
      case message: TextMessage.Streamed =>
        message.textStream.runForeach(println)
      case message: BinaryMessage =>
        message.dataStream.runWith(Sink.ignore)
    }.toMat(Sink.last)(Keep.right)

  val commandMessages = Seq(TextMessage("{\"op\":\"ping\"}"), TextMessage("{\"op\":\"unconfirmed_sub\"}"))

  val outgoing: Source[Strict, Promise[Option[Nothing]]] =
    Source(commandMessages.to[scala.collection.immutable.Seq]).concatMat(Source.maybe)(Keep.right)

  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(settings.RSVP_STREAM))


  val ((completionPromise, upgradeResponse), closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.both)
      .toMat(incoming)(Keep.both)
      .run()


  //  val flow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] =
  //    Http().webSocketClientFlow(WebSocketRequest(constants.RSVP_STREAM))
  //
  //  val (upgradeResponse, closed) =
  //    source
  //      .viaMat(flow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
  //      .toMat(sink)(Keep.both) // also keep the Future[Done]
  //      .run()

//  val flow: Flow[Message, Message, Promise[Option[Message]]] =
//    Flow.fromSinkAndSourceMat(
//      Sink.foreach[Message]({case TextMessage.Strict(s) => {
//        println(s)
//        producer.produce(topic, s)
//
//      } }),
//      Source.maybe[Message])(Keep.right)


  val connected = upgradeResponse.map { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      //println(upgrade.response)
      Future.successful(Done)
      //Done

    } else {
      Future.failed(new Exception(s"Connection failed: ${upgrade.response.status}"))
      producer.close()
    }
  }

  //connected.onComplete(Log.info)

  connected.onComplete(println)
  closed.foreach(_ => {
    println("closed")
    system.terminate
  })

  producer.close()

}