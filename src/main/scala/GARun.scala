import scala.collection.mutable.ListBuffer
import scala.util.Random

object GARun extends App {

}

case class GARun(champions: Champions) {
  val chromosomes: List[Chromosome] = initChromosomes(champions)
  var map = Map[String, List[Chromosome]]()

  map += ("immature" -> Nil)
  map += ("mature" -> chromosomes)


  var championIds: List[Int] = FileHelper.readCSVFileToList("champs.csv")


  /**
    * initiate the first generation
    *
    * @return
    */
  def initChromosomes(champions: Champions): List[Chromosome] = {
    var list: List[Chromosome] = List()
    for (i <- 0 to GAConfiguration.sample_number) {
      list = list.+:(buildChromosome(champions))
    }
    return list
  }

  def getRandomChampionId(pos: String, existChampionIds: List[Int]) = { //TODO:considering the pos

    var newChampionId = 0
    while ( {
      existChampionIds.contains(newChampionId) || newChampionId <= 0
    }) {
      val newChampionIndex = Random.nextInt(GAConfiguration.championIndex)
      newChampionId = championIds(newChampionIndex)
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
    val map = champions.map.map(x => {
      val championId = getRandomChampionId(x._1,existingChampionIds)
      (x._1,Champion(championId,0L))
    })
    val newChampions = Champions(map)
    Chromosome(newChampions,Fitness.getCollocationDegreeByGenes(newChampions))
  }

   def getAverageDegree(mature: List[Chromosome]):Double = {
    val list  = mature.sortBy(_.fitness).slice(0,mature.length-300).map(_.fitness)
    list.sum/list.length
  }

  /**
    * get all solutions
    *
    * @return
    */
  def getSolutions: List[Chromosome] = {
    var averageFitness = getAverageDegree(map("mature"))
    val isPrint = false
    var newFitness = 0
    var i =0
    while ( i< GAConfiguration.recursiveTimes) {
      if(i%15 == 0 && i!=0){
        map = breedAndSelection(map)
      }else{
        map = breed(map)
      }
      map = mature(map)
      map = survive(map)
      val newFitness = getAverageDegree(map("mature"))
      averageFitness = newFitness
      i +=1
    }
    val chromosomes = (map("mature") ++ map("toBeMature")).sortBy(_.fitness)
    System.out.println(averageFitness)
    chromosomes.slice(0, GAConfiguration.sample_number)
  }

  def survive(map:Map[String, List[Chromosome]]): Map[String, List[Chromosome]] = {
    map + ("mature"-> survive(map("mature")))

  }

  private def survive(list: List[Chromosome]):List[Chromosome] = {
    var result:List[Chromosome] = List()
    val source = ListBuffer() ++ list
    while (result.length < GAConfiguration.sample_number / 2) {
      val r1 = Random.nextInt(list.length)
      val r2 = Random.nextInt(list.length)
      val c1 = list(r1)
      val c2 = list(r2)
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
    val list = map("mature").sortBy(_.fitness)
    // divide into to parts
    val dividerIndex = Math.max(1, list.size*GAConfiguration.crossSolutionProp.intValue())
    val crossChromosomes = list.slice(0, dividerIndex)
    val copyChromosomes = list.slice(dividerIndex, list.size)
    val newGenerations = Reproduction.getNextGeneration(crossChromosomes)
    val immatures = copyChromosomes ++ newGenerations
    map + ("immature"->immatures)
  }

  def breedAndSelection(map: Map[String, List[Chromosome]]):Map[String, List[Chromosome]]={
    val list = map("mature").sortBy(_.fitness)
    val newGenerations = Reproduction.getNextGeneration(list)
    val immatures =  newGenerations
    map + ("immature"->immatures)
  }
}
