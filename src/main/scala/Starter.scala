import akka.actor.{ActorSystem, Props}
import akka.util.Timeout

object Starter extends App{
  override def main(args:Array[String])={
//    Crawler.crawl(1)
//implicit val timeout: Timeout = 10 second
    implicit val system = ActorSystem("MatchActor")
    val actor = system.actorOf(Props.create(classOf[MatchActor]), "account")
    actor ! "account/2019_4_13_account"
  }

}
