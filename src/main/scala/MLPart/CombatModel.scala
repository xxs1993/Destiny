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






    // model



    // predict and test


    spark.stop()
  }
}