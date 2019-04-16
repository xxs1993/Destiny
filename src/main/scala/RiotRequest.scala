import java.sql.Timestamp
import java.text.NumberFormat
import java.time.LocalDateTime

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.{DataFrame, Row}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.google.gson.{JsonArray, JsonObject, JsonParser}

import scala.util.parsing.json.JSON


trait RiotRequest{
  val config= ConfigFactory.load()
  val apikey = config.getString("riot.apikey ")
  val parser = new JsonParser()
  protected val log = LoggerFactory.getLogger(this.getClass.getName)

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
  def transJsonToRow(str: String):Row

}



/**
  * request account
  * @param rs
  */
case class RiotAccountRequest(rs:Seq[String]) extends RiotRequest {
//  private val log = LoggerFactory.getLogger(this.getClass.getName)

  val accountUrl = s"""https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/XXSSXX2020?api_key=$apikey"""
  val names = rs



   override def transJsonToRow(json:String):Row={
    val obj = parser.parse(json).asInstanceOf[JsonObject]
    Row(obj.get("name").toString,obj.get("id").toString,obj.get("accountId").toString)
  }

  /**
    * get Account info
    * @return
    */
  def requestForAccount():Future[Seq[Row]]={
     log.info("Received names of length : "+names.distinct.length)
     val r = for(s<-names.distinct )yield getURLContent(accountUrl.replace("XXSSXX2020",s.substring(s.indexOf("=")+1)).replace("+","_"))
     for(x<-Future.sequence(r))yield x.filter(_ !=null)

  }
}


/**
  * request match details
  * @param matchIds
  */
case class RiotMatchInfoRequest(matchIds:List[String]) extends RiotRequest {
  
  val url = s"""https://na1.api.riotgames.com/lol/match/v4/matches/gameId?api_key=$apikey"""

   def transJsonToRow(json: String): Row = {
    val obj = JSON.parseFull(json)
      .get.asInstanceOf[Map[String,Any]]
    val formatter = NumberFormat.getNumberInstance()
    val gameId = formatter.format((obj get "gameId" get).asInstanceOf[Double]).replace(",","")
    val tl = obj.get("teams").get.asInstanceOf[List[Map[String,Any]]].map(x=>(x.get("teamId").get->
        x.get("win").get.asInstanceOf[String]
    )).toMap
    val pl = obj.get("participants").get.asInstanceOf[List[Map[String,Any]]].map(
      x=>Map("teamId"->x.get("teamId").get,"championId"->x.get("championId").get,"position"->getPosition(x.get("timeline").get.asInstanceOf[Map[String,Any]])))
     val invalidItem = pl.filter(x=>{x.get("position") match {
       case None =>true
       case Some(x) => x.asInstanceOf[String].contains("NONE")
     }})
     if(invalidItem.length >0 ) return null
    val map:Map[String,Any] = pl.map(x=> {
      val teamId = x.get("teamId").get
      val result = tl.get(teamId).get.toString
      (tl.get(teamId).get+"_"+x.get("position").get)->x.get("championId").get.asInstanceOf[Double].intValue()}).toMap
     if(map.size!=10) return null
     println(map)
    Row(gameId,map.get("Win_TOP").get,map.get("Win_JUG").get,map.get("Win_MID").get,map.get("Win_BOT").get,map.get("Win_SUP").get,
      map.get("Fail_TOP").get,map.get("Fail_JUG").get,map.get("Fail_MID").get,map.get("Fail_BOT").get,map.get("Fail_SUP").get)
  }
  def getPosition(map:Map[String,Any]):String={
    val lane = map.get("lane").get.toString.replace("BOTTOM","BOT").replace("JUNGLE","JUG").replace("JUGGLE","JUG").replace("MIDDLE","MID")
    lane match {
      case x if !x.equals("BOT") =>x
      case other => map.get("role").get.toString match{
        case s if s.equals("DUO_SUPPORT")=> "SUP"
        case _ => other
      }
    }
  }

  def requestForMatchInfo():Future[List[Row]]={
    val result = for(id <- matchIds) yield getURLContent(url.replace("gameId",id.toString))
    Future.sequence(result)
  }
}

/**
  * request match id
  * @param df
  */
case class RiotMatchIdRequest(df:DataFrame) {
  private val log = LoggerFactory.getLogger(this.getClass.getName)
  val config= ConfigFactory.load()
  val apikey = config.getString("riot.apikey ")
  val timestamp = Timestamp.valueOf(LocalDateTime.now().plusWeeks(-1)).getTime

  val url = s"""https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/accountId?queue=420&seanson=13&beginTime=$timestamp&api_key=$apikey"""
  val rankQueueId = 420
  def getURLContent(u:String): Future[List[String]] = {
    Thread.sleep(1201)
    for {
      source <- Future(Try{Source.fromURL(u)})
    } yield {
      source match {
        case Success(v) =>transJsonToRow(v.mkString)
        case Failure(e) =>log.error(e.getMessage + s" -- $u");Nil
      }
    }
  }
  def transJsonToRow(json: String):List[String]={
    val obj = JSON.parseFull(json).get.asInstanceOf[Map[String,List[Map[String,Any]]]]
    val matches = obj.get("matches").get
    val formatter = NumberFormat.getNumberInstance

    for(o <-matches )yield  formatter.format(o.get("gameId").get.asInstanceOf[Double]).replace(",","")
  }


  def requestForMatchId():Future[List[String]]={
    log.info("start to request match id with accounts")
     val accounts = df.select("accountId").rdd.collect().toList
     val matchIds = for(a <- accounts ) yield getURLContent(url.replace("accountId",a.getString(0)))
     val sequence = Future.sequence(matchIds)
     for(f<-sequence if f!=null)yield f.flatten
  }


}
case class SummonerInfo(){

}
case class RiotChampionsRequest(name:String) extends RiotRequest {
  val championsListURL = s"""https://na1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/summonerId?api_key=$apikey"""

  override def transJsonToRow(str: String): Row = {
    val array = JSON.parseFull(str).asInstanceOf[List[Map[String,Any]]]
    val arr = array.map(x => (x.get("championid").get.asInstanceOf[Double].intValue()->x.get("championPoints").get.asInstanceOf[Double]))
    Row(arr)
  }

  def requestForChampionsBySummoners(): Future[List[(Int, Double)]] ={
    for(x <- getURLContent(championsListURL.replace("summonerId",name))) yield x.get(0).asInstanceOf[List[(Int,Double)]]
  }
}

//case class RiotChampionRequest(name:String) extends RiotRequest{
//  val url = s"""https://na1.api.riotgames.com/lol/league/v4/positions/by-summoner/summonerId?api_key=$apikey"""
//
//  override def transJsonToRow(str: String): Row = {
//    val arr =
//  }
//}
