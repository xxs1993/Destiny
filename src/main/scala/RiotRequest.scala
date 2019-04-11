import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.google.gson.{JsonObject, JsonParser}



case class RiotRequest(rs:Seq[String]){
  private val log = LoggerFactory.getLogger(this.getClass.getName)
  val config= ConfigFactory.load()
  val apikey = config.getString("riot.apikey ")
  val accountUrl = s"""https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/XXSSXX2020?api_key=$apikey"""
  val names = rs

  def getURLContent(u:String): Future[Row] = {
    Thread.sleep(1201)
    for {
      source <- Future(Try{Source.fromURL(u)})
    } yield {
      source match {
        case Success(v) =>transJsonToRow(v.mkString)
        case Failure(e) =>log.error(e.getMessage + s" -- $u");null
      }
    }
  }

  def transJsonToRow(json:String):Row={
    val parser = new JsonParser()
    val obj = parser.parse(json).asInstanceOf[JsonObject]
    Row(obj.get("name").toString,obj.get("id").toString,obj.get("accountId").toString)
  }
  def requestForAccount():Future[Seq[Row]]={
     val r = for(s<-names )yield getURLContent(accountUrl.replace("XXSSXX2020",s.substring(s.indexOf("=")+1)).replace("+","_"))
     for(x<-Future.sequence(r))yield x.filter(_ !=null)
  }
}
object RiotRequest {

}
