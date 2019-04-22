package MLPart

import org.apache.spark.sql.SparkSession


object WinANN extends App {


  override def main (args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("WinANN")
      .getOrCreate()

    // data processing
    val raw = spark.read.format("csv").option("header", "true").load("data/sample_data.csv")
    raw.show(10)
    raw.printSchema()

    val winCha = raw.select("Win_TOP", "Win_JUG", "Win_MID", "Win_BOT", "Win_SUP")



    // model


    // predict



    spark.stop()
  }

}