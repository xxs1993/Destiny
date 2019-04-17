import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest._
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Seconds, Span}

class RiotChampionMasteryRequestTest extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside {

  "testRequestForChampionMastery" should " success " taggedAs Slow in {
    val sc = SummonerChampion("3019212635","MePd72OWXzsXnSoAnof3M2uwDAJvay38-8QJhEw5FefAy-E",98,0)
    whenReady(RiotChampionMasteryRequest(List(sc)).requestForChampionMastery(),timeout(Span(10,Seconds))){
      w=> w.length==1
    }
  }

}
