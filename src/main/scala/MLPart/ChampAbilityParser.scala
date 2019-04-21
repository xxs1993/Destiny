package MLPart

import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat, udf}
import org.apache.spark.sql.catalyst.encoders.RowEncoder


object ChampAbilityParser extends App{


  override def main(args: Array[String]): Unit ={

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("ChampAbilityParser")
      .getOrCreate()


    val rate = WinRate().predictWinRate(List(92,120,55,22,555,266,104,99,222,40))

    println(rate)


    spark.stop()

  }

}