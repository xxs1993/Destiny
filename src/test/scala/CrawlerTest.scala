import Crawler.{crawler, opgg}
import org.scalatest._
import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class CrawlerTest  extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside {
    val good = """https://na.op.gg/summoner/userName=CastroDistrict"""
    val bad = """https://na.op.gg/summoner/userName="""


  "testGetURLContent" should " succeed" taggedAs Slow in {
    val goodCon = Crawler.getURLContent(good)
    whenReady(goodCon,timeout(Span(6, Seconds))) {w=>w.length>0 shouldBe true}
  }

  "testGetURLContent" should " not succeed" taggedAs Slow in {
    val badCon = Crawler.getURLContent(bad)
    intercept[Exception] {
      whenReady(badCon, timeout(Span(6, Seconds))) { e => e shouldBe a[NullPointerException] }
    }
  }

  "test wget" should "not succeed" taggedAs Slow in {
    val badUrl = Crawler.wget(bad)
    intercept[Exception]{
      whenReady(badUrl,timeout(Span(10,Seconds))){e=>e shouldBe a[Exception]}
    }
  }

  "test wget" should "succeed" taggedAs Slow in {
    val goodUrl = Crawler.wget(good)

    whenReady(goodUrl,timeout(Span(120,Seconds))){w=>w.length>0 shouldBe true}
  }

  "test Crawler" should " succeed" taggedAs Slow in{
    val re = Crawler.crawler(2,good)

    whenReady(re,timeout(Span(300,Seconds))){w=>w.collect().length should be >1}
  }

  "test Crawler" should " not succeed" taggedAs Slow in{
    val re = Try{Crawler.crawler(1,bad)}
    re.failure.exception shouldBe a[Exception]
  }
//  test("testWget") {
//
//  }
//
//  test("testCrawler") {
//
//  }
//
//  test("testCleanseHtml") {
//
//  }

}
