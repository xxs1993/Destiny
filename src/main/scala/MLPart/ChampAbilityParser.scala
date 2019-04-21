package MLPart

import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat, udf}
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

    val champion_data = spark.read.format("csv").option("header", "true").load("champs.csv")
    val champion_info = champion_data.map(
      r => (
        r.getAs[String]("id"),
        r.getAs[String]("P").toCharArray.map(a => a.toInt - 48).toList,
        r.getAs[String]("Q").toCharArray.map(a => a.toInt - 48).toList,
        r.getAs[String]("W").toCharArray.map(a => a.toInt - 48).toList,
        r.getAs[String]("E").toCharArray.map(a => a.toInt - 48).toList,
        r.getAs[String]("R").toCharArray.map(a => a.toInt - 48).toList
      )).toDF("champ_id", "p", "q", "w", "e", "r")
    var champ_info_map: Map[Int, Seq[Int]] = Map()
    champion_info.collect()
      .foreach(r => champ_info_map += (r.getAs[String]("champ_id").toInt -> Seq(
        r.getAs[Seq[Int]]("p"),
        r.getAs[Seq[Int]]("q"),
        r.getAs[Seq[Int]]("w"),
        r.getAs[Seq[Int]]("e"),
        r.getAs[Seq[Int]]("r")
      ).flatten))


    val model = MultilayerPerceptronClassificationModel.load("models/team_ann")
    val champs = Seq(150,20,157,29,161,75,203,7,110,43)
    val champs_with_a = champs.flatMap(n => champ_info_map(n))

    val arrToVec = udf((seq: Seq[Int]) => {
      Vectors.dense(seq.map(_.toDouble).toArray)
    })
    val test = spark.createDataFrame(Seq(
      (0.0, champs_with_a)
    )).toDF("e", "features").withColumn("features", arrToVec($"features"))

    val res = model.transform(test).drop("features").drop("e")
    res.show(false)
    res.printSchema()


    spark.stop()

  }

}