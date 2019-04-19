import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout

object Starter extends App{
  override def main(args:Array[String])={
//    Crawler.crawl(3)
//implicit val timeout: Timeout = 10 second
    implicit val system = ActorSystem("MatchActor")
    val actor = system.actorOf(Props.create(classOf[MatchActor]), "account")
    executeMatch(actor)
  }
  def executeMatch(actor: ActorRef): Unit ={
    actor ! "account/2019_4_15_account"
  }
  def executeChampionMastery(actor: ActorRef): Unit ={
    val sc = SummonerChampion("3019212635","MePd72OWXzsXnSoAnof3M2uwDAJvay38-8QJhEw5FefAy-E",98,0)
    actor ! List(sc)
  }

}
