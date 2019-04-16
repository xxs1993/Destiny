package MLPart

import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, OneHotEncoderEstimator}
import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.functions.collect_list
import org.apache.spark.sql.functions.udf
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

    // read in data and transform to DataFrame
    val participants = spark.read.format("csv").option("header", "true").load("data/participants.csv")
    val stats1 = spark.read.format("csv").option("header", "true").load("data/stats1.csv")
    val stats2 = spark.read.format("csv").option("header", "true").load("data/stats2.csv")

    // process data into the dataframe
    val stats = stats1.union(stats2)
    val statsWin = stats.selectExpr("cast(id as int) id", "cast(win as double ) win")
    val parts = participants.selectExpr("cast(id as int) id", "cast(championid as double) championid","cast(matchid as int) matchid")

    val partsWin = parts.join(statsWin, "id").groupBy("matchid","win").agg(collect_list("championid") )
    val data = partsWin
      .sort("matchid")
      .drop("matchid")
      .withColumnRenamed("collect_list(championid)", "features")
      .withColumnRenamed("win", "label")
      .na.drop()
    println(s"total lines of data (dropped null rows): ${data.count()}")

//    data.show(false)
//    data.printSchema()

    val arrToVec = udf((seq: Seq[Double]) => {
      Vectors.dense(seq.toArray)
    })
    val temp = data.withColumn("features", arrToVec(data.apply("features")))
    temp.printSchema()
//    temp.show(20, false)
    val df = temp.limit(1000) // limit to 100000 rows

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
    val layers = Array[Int](5, 10, 10, 2)
    val trainer = new MultilayerPerceptronClassifier()
        .setLayers(layers)
        .setSeed(1234L)
        .setMaxIter(10000)

    // fit model
    val model = trainer.fit(train)


    // save model


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
