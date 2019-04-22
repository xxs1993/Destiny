import javax.annotation.MatchesPattern
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}


class ChampionPickTest extends FlatSpec with Matchers with BeforeAndAfter{

  behavior of "ChampionPickTest"

  it should ("pickChampionForPos") in {
    val list = ChampionPick.pickChampionForPos("mid")
    list.length>0 shouldBe true

  }

  it should "pick nothing " in {
    ChampionPick.pickChampionForPos("") match {
      case Nil =>
    }
  }

}
