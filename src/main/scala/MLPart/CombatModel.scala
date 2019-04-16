package MLPart


import org.apache.spark.sql.SparkSession

object CombatModel extends App {

  override def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("CombatModel")
      .getOrCreate()

    // data manipulation
    val stats1 = spark.read.format("csv").option("header", "true").load("data/stats1.csv")
    val stats2 = spark.read.format("csv").option("header", "true").load("data/stats2.csv")
    val teamstats = spark.read.format("csv").option("header", "ture").load("data/teamstats.csv")

    val stats = stats1.union(stats2)





    // model



    // predict and test


    spark.stop()
  }
}