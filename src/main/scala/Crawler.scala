
import java.net.URL

import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.language.postfixOps
import scala.util._
import scala.util.control.Exception
import scala.xml.{Elem, Node, NodeSeq, XML}

/**
  * crawl user data
  */
object Crawler extends App {
  val opgg = """https://na.op.gg/summoner/userName="""
  val path = "/summoner/userName="
  val log = LoggerFactory.getLogger("Crawler")

  /*
  get html content by url
   */
  def getURLContent(u:String): Future[String] = {
    for {
      source <- Future(Source.fromURL(u))
    } yield cleanseHtml(source.mkString)
  }

  /*
  get the needed block of html, eliminate the disturb of javascript
   */
  def cleanseHtml(s:String):String= {
    val start = s.lastIndexOf("""<div class="FollowPlayers Names""")
    val end = s.lastIndexOf("""<div class="StatsButton">""")
    if(start<0 || end<0) {
//      log.warn(s)
      return null
    }
    s.substring(start,end).replace("javascript:","")
  }

  /**
    * get related links
    * @param u
    * @return
    */
  def wget(u: String): Future[Seq[String]] = {
    def getURLs(ns: Elem): Seq[String] = for (x <- ns \\ "a" map (_ \@ "href") if x.contains(path)&&(!x.contains(u)) ) yield "https:"+x
    def getLinks(g: String): Try[Seq[String]] ={
       g match {
         case null =>Failure(new RuntimeException("Null content "+u))
         case _ => Try{
              val elem = XML.loadString(g)
              getURLs(elem)
          }
         }
       }
      for (x <- getURLContent(u); s <- MonadHelper.asFuture(getLinks(x)) if(x!=null)) yield s
  }


  /**
    * keep crawling the link
    * @param depth
    * @param url
    * @return
    */
  def crawler(depth:Int, url:String): Future[RDD[String]] = {
      def inner(depth:Int,start:RDD[String],rt:RDD[String]):Future[RDD[String]]={
        Thread.sleep(1000)
          if(depth > 0){
            val newRu = Future.sequence(for( u<-start.collect().toSeq) yield wget(u))
            val newRru:Future[Seq[String]] = for(x<-newRu)yield x.flatten
            for(ru<-RDDHelper.transfer(newRru); i<-inner(depth-1,ru,rt ++ ru) if(ru !=null && rt!=null))yield i
        }else{
          return Future.successful(rt)
        }
      }
      val init = Await.result(RDDHelper.transfer(for(x<-wget(url))yield x),Duration("10 second"))
      inner(depth,init,init )
  }
  val a = Await.result(crawler(3,opgg+"CastroDistrict"),Duration("300 second"))
  FileHelper.writeToFile(RDDHelper.toDataFrame(a),"123.csv")

}
