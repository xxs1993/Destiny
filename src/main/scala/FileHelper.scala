import org.apache.spark.sql.{DataFrame, Encoder, Row}

import scala.util.Try

object FileHelper {
  def writeToFile(df:DataFrame,filepath:String)={
    df.write.format("com.databricks.spark.csv").save(filepath)
  }

  def writeToFile(seq:Seq[Row],filepath:String)(implicit encoders: Encoder[Row])={
    RDDHelper.spark.createDataset(seq).write.format("com.databricks.spark.csv").save(filepath)
  }

  def readCSVFile(filePath:String):Try[DataFrame]={
    Try{RDDHelper.spark.read.csv(filePath)}
  }
}
