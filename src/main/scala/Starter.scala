import akka.actor.{ActorRef, ActorSystem, Props}

object Starter extends App{
  override def main(args:Array[String])={
//    Crawler.crawl(3)
//implicit val timeout: Timeout = 10 second
    implicit val system = ActorSystem("MatchActor")
    val actor = system.actorOf(Props.create(classOf[MatchActor]), "account")
    executeMatchId(actor)
  }
  def executeMatch(actor: ActorRef): Unit ={
    actor ! "account/2019_4_15_account"
  }
  def executeChampionMastery(actor: ActorRef): Unit ={
    val sc = SummonerChampion("3019212635","MePd72OWXzsXnSoAnof3M2uwDAJvay38-8QJhEw5FefAy-E",98,0)
    actor ! List(sc)
  }

  def executeMatchId(actor:ActorRef)={
    val list = FileHelper.readCSVFile("match/matchidtest.csv").get.rdd.collect().toList.map(x=>x.getLong(0).toString)
    actor ! list
  }

}
