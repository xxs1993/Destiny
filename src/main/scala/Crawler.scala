

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.util._
import scala.xml.{Elem, XML}


/**
  * crawl user data from opgg
  */
object Crawler  {
  val opgg = """https://na.op.gg/summoner/userName="""
  val path = "/summoner/userName="
  val log = LoggerFactory.getLogger("Crawler")

  val spark:SparkSession = SparkSession
    .builder()
    .appName("Destiny")
    .master("local[*]")
    .getOrCreate()
  val sc = SparkContext.getOrCreate()
  import spark.implicits._


  def transfer[X:ClassTag](fs:Future[Seq[X]]):Future[RDD[X]]={
    for(s <- fs) yield sc.parallelize(s.distinct)
  }


  def toDataFrame[X:ClassTag](rdd:RDD[String]):DataFrame={
    rdd.collect().toList.toDF()
  }
  /*
  get html content by url
   */
  def getURLContent(u:String): Future[String] = {

    for {
      source <- Future(Try{Source.fromURL(u)})
    } yield cleanseHtml(source match {
      case Success(v) => v.mkString
      case Failure(e) => log.error(e.getMessage);null
    })
  }

  /*
  get the needed block of html, eliminate the disturb of javascript
   */
  def cleanseHtml(s:String):String= {
    if(s == null || s.isEmpty) return ""
    val start = s.lastIndexOf("""<div class="FollowPlayers Names""")
    val end = s.lastIndexOf("""<div class="StatsButton">""")
    if(start<0 || end<0) {
      return ""
    }
    s.substring(start,end).replace("javascript:","")
  }

  /**
    * get related links
    * @param u
    * @return
    */
  def wget(u: String): Future[Seq[String]] = {
    def getURLs(ns: Elem): Seq[String] = {
      val list = ns \\ "a" map (_ \@ "href")
      for (x <- list if x!=null && x.contains(path)&&(!x.contains(u)) ) yield "https:"+x
    }
    def getLinks(g: String): Try[Seq[String]] ={
       g match {
         case x if x==null || x.isEmpty =>log.warn("null content: "+u);Success(Seq())
         case _ => Try{
              val elem = XML.loadString(g)
              getURLs(elem)
          }
         }
       }
      for (x <- getURLContent(u) ; s <- MonadHelper.asFuture(getLinks(x))) yield s
  }


  /**
    * keep crawling the link
    * @param depth
    * @param url
    * @return
    */
  def crawler(depth:Int, url:String): Future[RDD[String]] = {
      def inner(depth:Int,start:RDD[String],rt:RDD[String]):Future[RDD[String]]={
//        Thread.sleep(1000)
          if(depth > 0){
            val newRu = Future.sequence(for( u<-start.collect().toSeq) yield wget(u))
            val newRru:Future[Seq[String]] = for(x<-newRu)yield x.flatten
            for(ru<-transfer(newRru); i<-inner(depth-1,ru,rt ++ ru) if(ru !=null && rt!=null))yield i
        }else{
          return Future.successful(rt)
        }
      }
      val init = Await.result(transfer(for(x<-wget(url))yield x),Duration("10 second"))
      inner(depth,init,init )
  }

  def crawl(depth:Int)={
    val a = Await.result(crawler(depth,opgg+"XXSSXX2020"),20 minute)
    log.info("Finished crawling the summoners names")
    //  val b = Await.result(RiotAccountRequest(a.collect().toList).requestForAccount(),Duration("200 second"))
    implicit val timeout: Timeout = 10 minute
    implicit val system = ActorSystem("MatchActor")
    val actor = system.actorOf(Props.create(classOf[MatchActor]), "account")
    actor ! a
  }


}
