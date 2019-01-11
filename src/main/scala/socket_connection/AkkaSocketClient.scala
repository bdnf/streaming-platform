import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Promise
import scala.io.StdIn

object AkkaSocketClient extends App {
  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()

  def flow: Flow[Message, Message, Any] = {
    val client = system.actorOf(Props(classOf[ClientConnectionActor]))
    //val in = Source.empty
    val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
      client ! ('income → a)
      a
    }
    Flow.fromSinkAndSource(Sink.foreach[Message]({case TextMessage.Strict(s) => {
      println(s)

    } }), out)
  }

//  val flow: Flow[Message, Message, Promise[Option[Message]]] =
//    Flow.fromSinkAndSourceMat(
//      Sink.foreach[Message]({case TextMessage.Strict(s) => {
//        println(s)
//
//      } }),
//      Source.maybe[Message])(Keep.right)

  val route = path("ws")(handleWebSocketMessages(flow))
  val bindingFuture = Http().webSocketClientFlow(WebSocketRequest(settings.RSVP_STREAM))


  import system.dispatcher

}

class ClientConnectionActor extends Actor {
  var connection: Option[ActorRef] = None

  val receive: Receive = {
    case ('income, a: ActorRef) ⇒ connection = Some(a); context.watch(a)
    case Terminated(a) if connection.contains(a) ⇒ connection = None; context.stop(self)
    case 'sinkclose ⇒ context.stop(self)

    case TextMessage.Strict(t) ⇒ connection.foreach(_ ! println(t))
    case _ ⇒ // ingone
  }

  override def postStop(): Unit = connection.foreach(context.stop)
}