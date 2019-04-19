package MLPart

import breeze.linalg.sum
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.encoders.RowEncoder


object DataAnalysis extends App {

  override def main(args: Array[String]): Unit ={

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("DataAnalysis")
      .getOrCreate()


    // ------------ data processing

    // make sure file is in the right format
    // may need to drop rows with null value
    val raw = spark.read.format("csv").option("header", "true").load("data/sample_data.csv")
    println(s"total lines - raw: ${raw.count()}")

    import spark.implicits._
//    case class MatchRecord(gameId: String,
//                           Win_TOP: Int, Win_JUG: Int, Win_MID: Int, Win_BOT: Int, Win_SUP: Int,
//                           Fail_TOP: Int, Fail_JUG: Int, Fail_MID: Int, Fail_BOT: Int, Fail_SUP: Int)

//    val encoder = RowEncoder(raw.schema)

//    val countChamp = raw.map(
//      s => List(
//        s.Win_TOP, s.Win_JUG, s.Win_MID, s.Win_BOT, s.Win_SUP,
//        s.Fail_TOP, s.Fail_JUG, s.Fail_MID, s.Fail_BOT, s.Fail_SUP))
//      .groupByKey(t => t).count().toDF("value", "count")


    // count picks of each champion
    // TODO: filter the one with total count < 10
    val countChamp = raw.map(
      s => List(
        s.getAs[String]("Win_TOP"),
        s.getAs[String]("Win_JUG"),
        s.getAs[String]("Win_MID"),
        s.getAs[String]("Win_BOT"),
        s.getAs[String]("Win_SUP"),
        s.getAs[String]("Fail_TOP"),
        s.getAs[String]("Fail_JUG"),
        s.getAs[String]("Fail_MID"),
        s.getAs[String]("Fail_BOT"),
        s.getAs[String]("Fail_SUP")))
      .flatMap(t => t)
      .groupByKey(t => t).count().toDF("champ_id", "count_total")
    println(s"total lines after filtering - countChamp: ${countChamp.count()}")

    // count all champion picks, 10 per match
    val sumCount = countChamp
      .selectExpr("cast(count_total as int) count")
      .collect()
      .map(r => r.getAs[Int]("count"))
      .sum

    // champion pick rate = champion / total pick
    val countChampRate = countChamp
      .selectExpr("cast(champ_id as int) champ_id", "cast(count_total as double) count_total")
        .withColumn("pick_rate", $"count_total"/sumCount)

    // champion pick count on win side
    val winChampCount = raw.map(
      s => List(
        s.getAs[String]("Win_TOP"),
        s.getAs[String]("Win_JUG"),
        s.getAs[String]("Win_MID"),
        s.getAs[String]("Win_BOT"),
        s.getAs[String]("Win_SUP")))
      .flatMap(t => t)
      .groupByKey(t => t).count().toDF("champ_id", "count_win")

    // champion pick count on fail side
    val failChampCount = raw.map(
      s => List(
        s.getAs[String]("Fail_TOP"),
        s.getAs[String]("Fail_JUG"),
        s.getAs[String]("Fail_MID"),
        s.getAs[String]("Fail_BOT"),
        s.getAs[String]("Fail_SUP")))
      .flatMap(t => t)
      .groupByKey(t => t).count().toDF("champ_id", "count_fail")

    // champion win rate
    val winChampRate = winChampCount
      .join(countChamp, "champ_id")
      .withColumn("rate_win", $"count_win"/$"count_total")

    // champion fail rate
    val failChampRate = failChampCount
        .join(countChamp, "champ_id")
        .withColumn("rate_fail", $"count_fail"/$"count_total")

    // champion win & fail count and rate
    val champWinFailCountRate = winChampCount
      .join(failChampCount, "champ_id")
      .join(countChamp, "champ_id")
      .withColumn("rate_win", $"count_win"/$"count_total")
      .withColumn("rate_fail", $"count_fail"/$"count_total")
      .withColumn("rate_pick", $"count_total"/sumCount)
    champWinFailCountRate.show()

//    var mapChampRate: Map[String, List[Double]] = Map()
//    champWinFailCountRate.foreach(r =>{
//      mapChampRate += (r.getAs[String]("champ_id")
//        -> List(r.getAs[Double]("rate_win"),
//        r.getAs[Double]("rate_fail"),
//        r.getAs[Double]("rate_pick")));println(mapChampRate)})




    // model


    // predict



    spark.stop()
  }

}