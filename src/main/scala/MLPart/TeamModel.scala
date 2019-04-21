package MLPart

import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, OneHotEncoderEstimator}
import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.functions.{collect_list, lit, udf}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * the model of given five champions, give prediction on the winning rate
  */
object TeamModel extends App {

  def process(df: DataFrame): Unit ={

  }


  override def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("ModelTraining")
      .getOrCreate()

    import spark.implicits._

    //     ================================================== sample data
    //    // read in data and transform to DataFrame
    //    val participants = spark.read.format("csv").option("header", "true").load("data/participants.csv")
    //    val stats1 = spark.read.format("csv").option("header", "true").load("data/stats1.csv")
    //    val stats2 = spark.read.format("csv").option("header", "true").load("data/stats2.csv")
    //
    //    // process data into the dataframe
    //    val stats = stats1.union(stats2)
    //    val statsWin = stats.selectExpr("cast(id as int) id", "cast(win as double ) win")
    //    val parts = participants.selectExpr("cast(id as int) id", "cast(championid as double) championid","cast(matchid as int) matchid")
    //
    //    val partsWin = parts.join(statsWin, "id").groupBy("matchid","win").agg(collect_list("championid") )
    //    val data = partsWin
    //      .sort("matchid")
    //      .drop("matchid")
    //      .withColumnRenamed("collect_list(championid)", "features")
    //      .withColumnRenamed("win", "label")
    //      .na.drop()
    //    println(s"total lines of data (dropped null rows): ${data.count()}")


    // read in champion attribute information and parse
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
    var champ_info_map: Map[String, Seq[Int]] = Map()
    champion_info.collect()
      .foreach(r => champ_info_map += (r.getAs[String]("champ_id") -> Seq(
        r.getAs[Seq[Int]]("p"),
        r.getAs[Seq[Int]]("q"),
        r.getAs[Seq[Int]]("w"),
        r.getAs[Seq[Int]]("e"),
        r.getAs[Seq[Int]]("r")
      ).flatten))

    val match_data = spark.read.format("csv").option("header", "true").load("data/team_match.csv")
    val match_data_reverse_label = match_data.map(
      r => (
        r.getAs[String]("Fail_TOP"),
        r.getAs[String]("Fail_JUG"),
        r.getAs[String]("Fail_MID"),
        r.getAs[String]("Fail_BOT"),
        r.getAs[String]("Fail_SUP"),
        r.getAs[String]("Win_TOP"),
        r.getAs[String]("Win_JUG"),
        r.getAs[String]("Win_MID"),
        r.getAs[String]("Win_BOT"),
        r.getAs[String]("Win_SUP")
      )).toDF("c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "c0").withColumn("label", lit(0))
    val match_data_label = match_data.drop("gameId")
      .withColumnRenamed("Win_TOP", "c1")
      .withColumnRenamed("Win_JUG", "c2")
      .withColumnRenamed("Win_MID", "c3")
      .withColumnRenamed("Win_BOT", "c4")
      .withColumnRenamed("Win_SUP", "c5")
      .withColumnRenamed("Fail_TOP", "c6")
      .withColumnRenamed("Fail_JUG", "c7")
      .withColumnRenamed("Fail_MID", "c8")
      .withColumnRenamed("Fail_BOT", "c9")
      .withColumnRenamed("Fail_SUP", "c0")
      .withColumn("label", lit(1))

    val match_label = match_data_label.union(match_data_reverse_label)
      .map(r => (Seq(
        champ_info_map(r.getAs[String]("c1")),
        champ_info_map(r.getAs[String]("c2")),
        champ_info_map(r.getAs[String]("c3")),
        champ_info_map(r.getAs[String]("c4")),
        champ_info_map(r.getAs[String]("c5")),
        champ_info_map(r.getAs[String]("c6")),
        champ_info_map(r.getAs[String]("c7")),
        champ_info_map(r.getAs[String]("c8")),
        champ_info_map(r.getAs[String]("c9")),
        champ_info_map(r.getAs[String]("c0"))
//        champion_info.filter($"champ_id" ===
//          r.getAs[String]("c1")).collect().toSeq.map(s => Seq(
//          s.getAs[Seq[Int]]("p"),
//          s.getAs[Seq[Int]]("q"),
//          s.getAs[Seq[Int]]("w"),
//          s.getAs[Seq[Int]]("e"),
//          s.getAs[Seq[Int]]("r")
//        ))
      ).flatten, r.getAs[Int]("label"))).toDF("features", "label").selectExpr("cast(label as Double) label", "features")

//    match_label.show(false)
//    match_label.printSchema()


    val arrToVec = udf((seq: Seq[Int]) => {
      Vectors.dense(seq.map(_.toDouble).toArray)
    })
    val temp = match_label.withColumn("features", arrToVec(match_label.apply("features")))
    temp.printSchema()
//    println(temp.count())

    def getRandom(dataset: DataFrame, n: Int) = {
      val count = dataset.count()
      val take = if (count > n) n else count
      dataset.sample(1.0*take/count).limit(take.toInt)
    }

    // temp.show(20, false)
    val df = getRandom(temp, 30000) // limit to n rows

    //    data.withColumn("vectors", as_vector("champions"))

        // vectorize features
    //    val encoder = new OneHotEncoderEstimator()
    //      .setInputCols("champions")
    //      .setOutputCol("features")


        val splits = df.randomSplit(Array(0.8, 0.2), seed = 1234L)
        val train = splits(0)
        val test = splits(1)

        // encode data


        // configure model
        // input layer of size 4 (features), two intermediate of size 5 and 4
        // and output of size 2 (classes)
        val layers = Array[Int](500, 200, 100, 70, 30, 15, 2)
        val trainer = new MultilayerPerceptronClassifier()
            .setLayers(layers)
            .setSeed(1234L)
    //        .setMaxIter(10000)

        // fit model
        val model = trainer.fit(train)


        // save model
        model.save("models/team_ann")


        // compute accuracy on the test set
        val result = model.transform(test)
    //    val tt = result.orderBy(result("label").desc)
    //    tt.show(40, false)
    //    result.printSchema()

        val predictionAndLabels = result.select("prediction", "label")

        val evaluator = new MulticlassClassificationEvaluator()
            .setMetricName("accuracy")

        println(s"Test set accuracy: ${evaluator.evaluate(predictionAndLabels)}")


    spark.stop()
  }

}
