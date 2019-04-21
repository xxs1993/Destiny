
package MLPart

import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.udf

object Predict extends App {

  override def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("Predict")
      .getOrCreate()

    val model = MultilayerPerceptronClassificationModel.load("models/team_ann")

    // 267
    val champions = Array(126,77,99,81,0,223,59,142,222,43)

    val absent = getAbsentPosition(champions)
    println(s"absent position: $absent")

    val champ_set = ChampionTag().getTagByPosition(absent)

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

    val champ_name = spark.read.format("csv").option("header", "true").load("data/champs.csv")
    var champ_name_map: Map[Int, String] = Map()
    champ_name.collect().foreach(r => champ_name_map += (r.getAs[String]("id").toInt -> r.getAs[String]("name")))

    val arrToVec = udf((seq: Seq[Int]) => {
      Vectors.dense(seq.map(_.toDouble).toArray)
    })
    var res: Map[Int, Double] = Map()
    for (to <- champ_name_map.keys) {
      if (!champions.contains(to)) {
        val impl = champions.map(n => if (n == 0) to else n)
        val champs_with_a = impl.flatMap(x => champ_info_map(x))

        val test = spark.createDataFrame(Seq(
          (0.0, champs_with_a)
        )).toDF("e", "features").withColumn("features", arrToVec($"features"))

        model.transform(test)
          .select("probability")
          .collect()
          .foreach({case Row(probability: Vector) => res += (to -> probability.apply(1))})
      }
    }

    println(res.filter(p => champ_set.contains(p._1)).maxBy(_._2))

    spark.stop()

  }

  def getAbsentPosition(cs: Array[Int]): String = {
    if(cs(0) == 0) "top"
    else if(cs(1) == 0) "jg"
    else if(cs(2) == 0) "mid"
    else if(cs(3) == 0) "bot"
    else if(cs(4) == 0) "sup"
    else ""
  }
}