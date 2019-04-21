package MLPart

import org.apache.spark.sql.SparkSession

case class ChampionTag(){

  def init(): Unit = {
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("ChampionTag")
      .getOrCreate()

    import spark.implicits._

    val raw = spark.read.format("csv").option("header", "true").load("data/team_match.csv")
    champ_position_map += ("top" -> (raw.selectExpr("cast(Win_TOP as Int) Win_TOP").map(r => r(0).asInstanceOf[Int]).collect()
      ++: raw.selectExpr("cast(Fail_TOP as Int) top").map(_(0).asInstanceOf[Int]).collect()).toSet)
    champ_position_map += ("jg" -> (raw.selectExpr("cast(Win_JUG as Int) jg").map(r => r(0).asInstanceOf[Int]).collect()
      ++: raw.selectExpr("cast(Fail_JUG as Int) jg").map(_(0).asInstanceOf[Int]).collect()).toSet)
    champ_position_map += ("mid" -> (raw.selectExpr("cast(Win_MID as Int) mid").map(r => r(0).asInstanceOf[Int]).collect()
      ++: raw.selectExpr("cast(Fail_MID as Int) mid").map(_(0).asInstanceOf[Int]).collect()).toSet)
    champ_position_map += ("bot" -> (raw.selectExpr("cast(Win_BOT as Int) bot").map(r => r(0).asInstanceOf[Int]).collect()
      ++: raw.selectExpr("cast(Fail_BOT as Int) bot").map(_(0).asInstanceOf[Int]).collect()).toSet)
    champ_position_map += ("sup" -> (raw.selectExpr("cast(Win_SUP as Int) sup").map(r => r(0).asInstanceOf[Int]).collect()
      ++: raw.selectExpr("cast(Fail_SUP as Int) sup").map(_(0).asInstanceOf[Int]).collect()).toSet)

//    spark.stop()
  }

  var champ_position_map: Map[String, Set[Int]] = Map()
  init()

  def getTagByPosition(p: String): Set[Int] = {
    champ_position_map(p)
  }
}
