

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}




case class MatchActor() extends Actor with ActorLogging{
//  private val log = LoggerFactory.getLogger(this.getClass.getName)
//  implicit val timeout: Timeout = 10 second
//  implicit val system = ActorSystem("MatchActor")
//  val actor = system.actorOf(Props.create(classOf[MatchActor]), "match")
implicit val encoder = Encoders.javaSerialization(Row.getClass).asInstanceOf[Encoder[Row]]

  override def receive: Receive = {
    case x:RDD[String] =>{
      val spark:SparkSession = SparkSession
        .builder()
        .appName("Destiny")
        .master("local[*]")
        .getOrCreate()
      import spark.implicits._
      val accounts = Await.result(RiotAccountRequest(x.collect()).requestForAccount(),20 minute).map(x=>(x.getString(0),x.getString(1),x.getString(2))).toDF("name","id","accountId")
      val date = LocalDate.now()
      val fileName = "account/"+date.getYear+"_"+date.getMonthValue+"_"+date.getDayOfMonth+"_account"
      FileHelper.writeToFile(accounts,fileName)
      log.info("Finished writing account data to the file : "+fileName)
      spark.stop()
      self ! fileName
    }
    case x:String =>{
      log.info("Received message : "+x)
      val spark:SparkSession = SparkSession
        .builder()
        .appName("Destiny")
        .master("local[*]")
        .getOrCreate()
      import spark.implicits._
      val df = FileHelper.readCSVFile(x+"/*.csv") match {
        case Success(value) => value
        case Failure(e) => throw new RuntimeException("Cannot find the file : "+x,e)
      }
      val matchIds = Await.result(RiotMatchIdRequest(df).requestForMatchId(),40 minute)
      log.info("get all matchIds with length:  "+matchIds.length)
      val result = Await.result(RiotMatchInfoRequest(matchIds.distinct).requestForMatchInfo(),40 minute)
      log.info("get all match info with length : "+result.length)
      val date = LocalDate.now()
      val fileName = "match/"+date.getYear+"_"+date.getMonthValue+"_"+date.getDayOfMonth+"_match"
      val championMas = result.map(x=>x.getList(11))
      self ! championMas
      FileHelper.writeToFile(result.filter(x=>x!=null).map(x=>(x.getString(0),x.getInt(1),x.getInt(2),x.getInt(3),x.getInt(4),x.getInt(5),x.getInt(6),x.getInt(7),x.getInt(8),x.getInt(9),x.getInt(10)))
        .toDF("gameId","Win_TOP","Win_JUG","Win_MID","Win_BOT","Win_SUP","Fail_TOP","Fail_JUG","Fail_MID","Fail_BOT","Fail_SUP"),fileName)
      spark.stop()
    }
    case l:List[SummonerChampion]=>{
      log.info("Start to request Champion mastery")
      val result = Await.result(RiotChampionMasteryRequest(l).requestForChampionMastery(),1 minute)
      val spark:SparkSession = SparkSession
        .builder()
        .appName("Destiny")
        .master("local[*]")
        .getOrCreate()
      import spark.implicits._
      val date = LocalDate.now()
      val fileName = "match/"+date.getYear+"_"+date.getMonthValue+"_"+date.getDayOfMonth+"_champion"
      FileHelper.writeToFile(result.filter(x=>x!=null).map(x=>(x.gameId,x.championId,x.point)).toDF("gameId","championId","point"),fileName)
      spark.stop()
    }
  }

}
