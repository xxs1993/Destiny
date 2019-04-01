import java.net.URL

import akka.actor.Actor

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class MatchActor() extends Actor{

  override def receive: Receive = {
    case input:Future[Seq[URL]]=>{
      val su = Await.ready(input,Duration("1 second") )
      for(u <- su){

      }
    }
  }
}
