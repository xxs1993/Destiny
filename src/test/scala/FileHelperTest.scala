
import org.apache.spark.sql.SparkSession
import org.scalatest._

import scala.reflect.io.File

class FileHelperTest extends  FlatSpec with Matchers with BeforeAndAfter {

  implicit var spark:SparkSession = _
  before{
     spark = SparkSession
      .builder()
      .appName("Destiny")
      .master("local[*]")
      .getOrCreate()

    val file =  File("text")
    if(file.exists){
      file.delete()
    }
  }
  after {
    if (spark != null) {
      spark.stop

    }
  }


 "test readCsv " should "Success" in {
   val result =  spark.read.format("csv").option("inferSchema","true").option("header","true").load("*.csv")
   result.select("account").count() shouldBe 4
//   result.coalesce(1).write.option("header","true").csv("text/1.csv")
 }

  "test writeToCSV" should "success" in{
    val result =  spark.read.format("csv").option("inferSchema","true").option("header","true").load("*.csv")
    result.coalesce(1).write.option("header","true").csv("text/1.csv")
    val newFile = File("text/1.csv")
    newFile.exists shouldBe true
  }

  "test nothing" should "success" in {
    val result =  spark.read.format("csv").option("inferSchema","true").option("header","true").load("account/2019_4_13_account/*.csv")
    result.foreach(print(_))
//    print(Seq("sss","sss"))

  }

}
