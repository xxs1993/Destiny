import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest._
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Seconds, Span}

class RiotMatchIdRequestTest extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside {
  val config= ConfigFactory.load()
  val apikey = config.getString("riot.apikey ")
  val url = s"""https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/t4SNxRsHMH0liujzN4paV0bQP1qKXiKeL9bu8IrVSVucGStFsusbgFLz?api_key=$apikey"""
  val spark = SparkSession.builder().master("local").getOrCreate()
  val list = List("t4SNxRsHMH0liujzN4paV0bQP1qKXiKeL9bu8IrVSVucGStFsusbgFLz")
  import spark.implicits._
  val df = list.toDF("accountId")
  val riotRequest = RiotMatchIdRequest(df)
  "getURLContent " should "success " taggedAs Slow in{
     whenReady(riotRequest.getURLContent(url),timeout(Span(10,Seconds))){w=>w.length >0}
  }

  "requestForMatchId" should "success" taggedAs Slow in {
    whenReady(riotRequest.requestForMatchId(),timeout(Span(10,Seconds))){w=>w.length>0}
  }


}
