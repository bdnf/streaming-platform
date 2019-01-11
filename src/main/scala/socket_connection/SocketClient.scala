package socket_connection

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.Socket

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.{Future, Promise}

import kafka_producer.RSVPKafkaProducer

object Transmitter extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  val producer = new RSVPKafkaProducer

  object Rec extends Actor {
    override def receive: Receive = {
      case TextMessage.Strict(msg) =>
        //Log.info("Recevied signal " + msg)
        println("Recevied signal " + msg)
    }
  }
//  val sink: Sink[Message, Future[Done]] =
//  Sink.foreach {
//    case message: TextMessage.Strict =>
//      println(message.text)
//  }


  val sink: Sink[Message, NotUsed] = Sink.actorRef[Message](system.actorOf(Props(Rec)), PoisonPill)


  //val source: Source[Message, NotUsed] = Source.single(TextMessage(""))


//  val flow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] =
//    Http().webSocketClientFlow(WebSocketRequest(constants.RSVP_STREAM))
//
//  val (upgradeResponse, closed) =
//    source
//      .viaMat(flow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
//      .toMat(sink)(Keep.both) // also keep the Future[Done]
//      .run()

  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(
      Sink.foreach[Message]({case TextMessage.Strict(s) => {
        println(s)
        producer.produce(settings.PUBLISH_TOPIC, s)

      } }),
      Source.maybe[Message])(Keep.right)

  val (upgradeResponse, promise) =
    Http().singleWebSocketRequest(
      WebSocketRequest(settings.RSVP_STREAM),
      flow)

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
  //while(true)
  connected.onComplete(println)
  //closed.foreach(_ => println("closed"))

  //producer.close()

}