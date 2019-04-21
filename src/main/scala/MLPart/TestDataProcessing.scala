package MLPart

import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.{Row, SparkSession}

object TestDataProcessing extends App {
  import org.apache.spark.sql.functions._

  override def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("TestDataProcessing")
      .getOrCreate()

    val arrToVec = udf((seq: Seq[Double]) => {
      Vectors.dense(seq.toArray)
    })

    // import data
    val participants = spark.read.format("csv").option("header", "true").load("data/participants.csv")
    val stats1 = spark.read.format("csv").option("header", "true").load("data/stats1.csv")
    val stats2 = spark.read.format("csv").option("header", "true").load("data/stats2.csv")

    // process data into the dataframe
    val stats = stats1.union(stats2)
    val statsWin = stats.selectExpr("cast(id as int) id", "cast(win as double) win")
    val parts = participants.selectExpr("cast(id as int) id", "cast(championid as double) championid","cast(matchid as int) matchid")

    val partsWin = parts.join(statsWin, "id").groupBy("matchid","win").agg(collect_list("championid") )
    val data = partsWin.sort("matchid").drop("matchid").withColumnRenamed("collect_list(championid)", "features")
//    data.show(false)
//    data.printSchema()
    val dataVec = data.withColumn("features", arrToVec(data.apply("features")))
    dataVec.show(false)
    dataVec.printSchema()

    spark.stop()
  }
}
