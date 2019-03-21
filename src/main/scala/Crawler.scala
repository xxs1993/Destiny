
import java.net.URL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util._
import scala.xml.Node
import org.htmlcleaner.{HtmlCleaner, TagNode}
import scala.xml.parsing.XhtmlParser

object Crawler extends App {
  val opgg = """https://na.op.gg/summoner/userName="""
  def getURLContent(u:URL): Future[String] = {
//    var url = opgg + name
    for {
      source <- Future(Source.fromURL(u))
    } yield cleanseHtml(source.mkString)
  }

  def cleanseHtml(s:String):String= {
    val h = new HtmlCleaner();
    val html = h.getInnerHtml(h.clean(s))
    var str = ""
    str = html.replaceAll("<script>.*</script>","")
    return str
  }

//  def parse(u:URL): Unit ={
//       val parser = new XhtmlParser()
//  }



  def asFuture[X](xy: Try[X]): Future[X] = xy match {
    case Success(s) => Future.successful(s)
    case Failure(e) => Future.failed(e)
  }

  def wget(u: URL): Future[Seq[URL]] = {
    //    var url = new URL(u)
    def getURLs(ns: Node): Seq[URL] = for (x <- ns \\ "a" map (_ \@ "href")) yield new URL(u, x)

    def getLinks(g: String): Try[Seq[URL]] =
      for (n <- HTMLParser.parse(g) recoverWith { case f => Failure(new RuntimeException(s"parse problem with URL $u: $f")) })
        yield getURLs(n)

    for (x <- getURLContent(u); s <- asFuture(getLinks(x))) yield s
  }
  val urls = Await.result(wget(new URL(opgg+"XXSSXX2020")),10 seconds)
  print(urls.length)

//  def wget(us: Seq[URL]): Future[Seq[Either[Throwable, Seq[URL]]]] = {
//
//  }
//
//  def crawler(depth: Int, args: Seq[URL]): Future[Seq[URL]] = {
//    def inner(urls: Seq[URL], depth: Int, accum: Seq[URL]): Future[Seq[URL]] =
//      if (depth > 0)
//        for (us <- MonadOps.flattenRecover(wget(urls), { x => System.err.println(x) }); r <- inner(us, depth - 1, accum ++: urls)) yield r
//      else
//        Future.successful(accum)
//    inner(args, depth, Nil)
//  }
//
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
