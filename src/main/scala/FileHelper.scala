import org.apache.spark.sql.DataFrame

import scala.util.Try

object FileHelper {
  def writeToFile(df:DataFrame,filepath:String)={
    df.write.format("com.databricks.spark.csv").save(filepath)
  }

  def readCSVFile(filePath:String):Try[DataFrame]={
    Try{RDDHelper.spark.read.csv(filePath)}
  }
}
