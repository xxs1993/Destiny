import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest._
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Seconds, Span}

class RiotAccountRequestTest extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside  {
//
val df = Seq(("t4SNxRsHMH0liujzN4paV0bQP1qKXiKeL9bu8IrVSVucGStFsusbgFLz"))
  val request = RiotAccountRequest(df)


  "requestForAccount" should " succeed" taggedAs Slow in {
    whenReady(request.requestForAccount(),timeout(Span(12,Seconds))){w=>w.length>0 shouldBe true}
  }


}
