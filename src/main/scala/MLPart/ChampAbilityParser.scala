package MLPart

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.encoders.RowEncoder


object ChampAbilityParser extends App{

  def parseToArray(atts: String): Unit ={

  }

  override def main(args: Array[String]): Unit ={

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("ChampAbilityParser")
      .getOrCreate()

    import spark.implicits._
    case class Record(champ_id: String, p: List[Int], q: List[Int], w: List[Int], e: List[Int], r: List[Int])

    val data = spark.read.format("csv").option("header", "true").load("champs.csv").limit(100)
//    data.show()
//    val encoder = RowEncoder(data.schema)
    val df = data.map(
      r => (
        r.getAs[String]("id"),
        r.getAs[String]("P").toCharArray.map(a => a.toInt-48).toList,
        r.getAs[String]("1").toCharArray.map(a => a.toInt-48).toList,
        r.getAs[String]("W").toCharArray.map(a => a.toInt-48).toList,
        r.getAs[String]("E").toCharArray.map(a => a.toInt-48).toList,
        r.getAs[String]("R").toCharArray.map(a => a.toInt-48).toList
    )).toDF("champ_id", "p", "q", "w", "e", "r")

//    data.foreach(
//      r => println(List(
//        r.getAs[String]("id"),
//        r.getAs[String]("P"),
//        r.getAs[String]("1"),
//        r.getAs[String]("W"),
//        r.getAs[String]("E"),
//        r.getAs[String]("R")
//      )))

    df.show(false)
    df.printSchema()



  }

}