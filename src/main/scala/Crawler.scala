
import java.net.URL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.language.postfixOps
import scala.util._
import scala.xml.Node

/**
  * crawl user data
  */
object Crawler extends App {
  val opgg = """https://na.op.gg/summoner/userName="""
  val path = "/summoner/userName="
  /*
  get html content by url
   */
  def getURLContent(u:URL): Future[String] = {
    for {
      source <- Future(Source.fromURL(u))
    } yield cleanseHtml(source.mkString)
  }

  /*
  get the needed block of html
   */
  def cleanseHtml(s:String):String= {
    val start = s.indexOf("""<div class="GameItemList"""")
    val end = s.indexOf("""<div class="GameMoreButton Box"""")
    s.substring(start,end).replace("javascript:","")
  }

  /**
    * get related links
    * @param u
    * @return
    */
  def wget(u: URL): Future[Seq[URL]] = {
    def getURLs(ns: Node): Seq[URL] = for (x <- ns \\ "a" map (_ \@ "href") if x.contains(path)&&(!x.contains(u.getPath)) ) yield new URL(u, x)
    def getLinks(g: String): Try[Seq[URL]] =
      for (n <- HTMLParser.parse(g) recoverWith { case f => Failure(new RuntimeException(s"parse problem with URL $u: $f")) })
        yield getURLs(n)
    for (x <- getURLContent(u); s <- MonadHelper.asFuture(getLinks(x))) yield s
  }

  def wget(us: Seq[URL]): Future[Seq[Either[Throwable, Seq[URL]]]] = {
    val us2 = us.distinct
    Future.sequence(for(u<- us2)yield MonadHelper.sequence(wget(u)))
  }
//
  var se = Seq[URL](new URL(""))

//  def crawler(depth: Int, args: Seq[URL]): Future[Stream[URL]] = {
//    def inner(urls: Seq[URL], depth: Int, accum: Seq[URL]): Future[Stream[URL]] =
//      if (depth > 0)
//        for (us <- MonadOps.flattenRecover(wget(urls), { x => System.err.println(x) }); r <- inner(us, depth - 1, accum ++: urls)) yield r
//      else
//        Future.successful(accum)
//    inner(args, depth, Nil)
//  }

//  println(s"web reader: ${args.toList}")
//  val urls = for (arg <- args toList) yield Try(new URL(arg))
//  val s = MonadOps.sequence(urls)
//  s match {
//    case Success(z) =>
//      println(s"invoking crawler on $z")
//      val f = crawler(2, z)
//      Await.ready(f, Duration("60 second"))
//      for (x <- f) println(s"Links: $x")
//    case Failure(z) => println(s"failure: $z")
//  }
}
