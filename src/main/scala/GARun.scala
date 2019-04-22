import MLPart.ChampionTag
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.util.Random

object GARun extends App {
  val spark:SparkSession = SparkSession
    .builder()
    .appName("GARun")
    .master("local[*]")
    .getOrCreate()
  val sc = SparkContext.getOrCreate()
  import spark.implicits._
  val map1 = Map("Win_TOP"->0,"Win_JUG"->2,"Win_MID"->0,"Win_BOT"->15,"Win_SUP"->16,
    "Fail_TOP"->23,"Fail_JUG"->0,"Fail_MID"->13,"Fail_BOT"->22,"Fail_SUP"->25)
  val map2 = Map("Win_TOP"->516,"Win_JUG"->427,"Win_MID"->142,"Win_BOT"->145,"Win_SUP"->555,
    "Fail_TOP"->86,"Fail_JUG"->120,"Fail_MID"->268,"Fail_BOT"->119,"Fail_SUP"->412)
  var cham1 = Champions(map1.map(x=>(x._1,Champion(x._2,10000))))
  var cham2 = Champions(map2.map(x=>(x._1,Champion(x._2,10000))))
  val pos = "Win_MID"
  val list = GARun(cham1).getSolutions
  list.slice(0,20).foreach(x=>{println(x.fitness);println(x.genes)})
  val df = list.map(x=>x.genes.map(pos).championId).toDF("id").groupBy("id").count().toDF("id","count").orderBy("count")
  df.show()

}

case class GARun(champions: Champions) {
  val spark:SparkSession = SparkSession
    .builder()
    .appName("GARun")
    .master("local[*]")
    .getOrCreate()
  val sc = SparkContext.getOrCreate()
  private val log = LoggerFactory.getLogger(this.getClass.getName)
  val tagMap = ChampionTag().champ_position_map
  val chromosomes= initChromosomes(champions)
  var map = Map[String, List[Chromosome]]()
  map += ("immature" -> Nil)
  map += ("mature" -> chromosomes)
  map+=("toBeMature"->Nil)

  /**
    * initiate the first generation
    *
    * @return
    */
  def initChromosomes(champions: Champions): List[Chromosome] = {
    val list: List[Champions] = List.fill(GAConfiguration.sample_number)(champions)
    list.map(x=>buildChromosome(x))
  }

  def getRandomChampionId(pos: String, existChampionIds: List[Int]) = { //TODO:considering the pos
    var newChampionId = 0
    while ( {
      existChampionIds.contains(newChampionId) || newChampionId <= 0
    }) {
      val list = tagMap(pos.split("_")(1).toLowerCase).toList
      val newChampionIndex = Random.nextInt(list.length)
      newChampionId = list(newChampionIndex)
    }
    newChampionId
  }


  /**
    * build single chromosome
    *
    * @return
    */
  def buildChromosome(champions: Champions): Chromosome = {
    val existingChampionIds = champions.map.values.map(x=>x.championId).toList
    val map = champions.map.map(x => x._2.championId match {
      case 0 => {
        val championId = getRandomChampionId(x._1, existingChampionIds)
        (x._1, Champion(championId, 0L))
      }
      case _ => x
    })
    val newChampions = Champions(map)
    Chromosome(newChampions,Fitness.getCollocationDegreeByGenes(newChampions))
  }

   def getAverageDegree(mature: List[Chromosome]):Double = {
    val list  = mature.sortBy(_.fitness).reverse.slice(0,mature.length-300).map(_.fitness)
    list.sum/list.length
  }

  /**
    * get all solutions
    *
    * @return
    */
  def getSolutions: List[Chromosome] = {
//    var averageFitness = getAverageDegree(map("mature"))
//    val isPrint = false
//    var newFitness = 0
    var i =0
    while ( i< GAConfiguration.recursiveTimes) {
      println(s"recursive : $i times" )
      if(i%15 == 0 && i!=0){
        map = breedAndSelection(map)
      }else{
        map = breed(map)
      }
      map = mature(map)
      map = survive(map)
//      val newFitness = getAverageDegree(map("mature"))
//      averageFitness = newFitness
      i +=1
    }
    val chromosomes = (map("mature") ++ map("toBeMature")).sortBy(_.fitness).reverse
//    println(averageFitness)
    chromosomes.slice(0, GAConfiguration.sample_number)
  }

  def survive(map:Map[String, List[Chromosome]]): Map[String, List[Chromosome]] = {
    map + ("mature"-> survive(map("mature")))

  }

   def survive(list: List[Chromosome]):List[Chromosome] = {
    var result:List[Chromosome] = List()
    val source = ListBuffer() ++ list
    while (result.length < GAConfiguration.sample_number / 2) {
      val r1 = Random.nextInt(source.length)
      val r2 = Random.nextInt(source.length)
      val c1 = source(r1)
      val c2 = source(r2)
      if (c1.fitness >= c2.fitness) {
        result = result.+:(c1)
        source.remove(r1)
      }
      else {
        result = result.+:(c2)
        source.remove(r2)
      }
    }
    result
  }

  def mature(map: Map[String, List[Chromosome]]): Map[String, List[Chromosome]] = {
    var result = map
    var matureList = map("mature")
    if (matureList == null) matureList = Nil
    val immatureList = map("toBeMature")
    if (immatureList != null) matureList = matureList ++ (immatureList)
    result+=("mature"->matureList,"toBeMature"-> map("immature"))
    result - ("immature")

  }

  /**
    * get next generations
    *
    * @param map
    * @return
    */
  def breed(map: Map[String, List[Chromosome]]): Map[String, List[Chromosome]] = {
    val list = map("mature").sortBy(_.fitness).reverse
    // divide into to parts
    val div = (list.size * (GAConfiguration.crossSolutionProp)).toInt
    val dividerIndex =  Math.max(1,div)
    val crossChromosomes = list.slice(0, dividerIndex)
    val copyChromosomes = list.slice(dividerIndex, list.size)
    val newGenerations = Reproduction.getNextGeneration(crossChromosomes)
    val immatures = copyChromosomes ++ newGenerations
    map + ("immature"->immatures)
  }

  def breedAndSelection(map: Map[String, List[Chromosome]]):Map[String, List[Chromosome]]={
    val list = map("mature").sortBy(_.fitness).reverse
    val newGenerations = Reproduction.getNextGeneration(list)
    val immatures =  newGenerations
    map + ("immature"->immatures)
  }
}
