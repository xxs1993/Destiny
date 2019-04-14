import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Encoder, Row, SparkSession}

import scala.util.Try

object FileHelper {

  def writeToFile(df:DataFrame,filepath:String)={
    val spark:SparkSession = SparkSession
      .builder()
      .appName("Destiny")
      .master("local[*]")
      .getOrCreate()
    df.repartition(1).write.option("header","true").option("quote","").option("escape","").csv(filepath)
    spark.stop()
  }

  def writeToFile(seq:Seq[Row],filepath:String)(implicit encoders: Encoder[Row])={
    val spark:SparkSession = SparkSession
      .builder()
      .appName("Destiny")
      .master("local[*]")
      .getOrCreate()
    spark.createDataset(seq).repartition(1).write.option("header","true").option("quote","").option("escape","").csv(filepath)
  }

  def readCSVFile(filePath:String):Try[DataFrame]={
    val spark:SparkSession = SparkSession
      .builder()
      .appName("Destiny")
      .master("local[*]")
      .getOrCreate()
    val result = Try{spark.read.format("csv").option("inferSchema","true").option("header","true").load(filePath)}
    return result
  }
}
