import java.net.URL

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.ScalaReflection.Schema
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

object RDDHelper {
  val spark:SparkSession = SparkSession
    .builder()
    .appName("Destiny")
    .master("local[*]")
    .getOrCreate()
  val sc = SparkContext.getOrCreate()
  import spark.implicits._

  def transfer[X:ClassTag](fs:Future[Seq[X]]):Future[RDD[X]]={
    for(s <- fs) yield sc.parallelize(s.distinct)
  }


 def toDataFrame[X:ClassTag](rdd:RDD[String]):DataFrame={
   rdd.collect().toList.toDF()
 }




}
