import MLPart.ChampionTag

import scala.collection.mutable.ListBuffer
import scala.util.Random

object Reproduction {
  val tagMap = ChampionTag().champ_position_map
  def getNextGeneration(list: List[Chromosome]): List[Chromosome] = {
    var i = 0
    val newList = list.map(x=>{
      val parents = Fitness.fitness(list)
      val fa = parents(0)
      val mo = parents(1)
      val son = getNextGeneration(fa,mo)
      val son2 = getNextGeneration(fa,mo)
      List(son,son2)
    }).flatten
    pick(newList)
  }

  def pick(list: List[Chromosome]): List[Chromosome] = {
    var result:List[Chromosome] = List()
    val length = list.length
    val source = ListBuffer() ++ list
    while (result.length < length/ 2) {
      val r1 = Random.nextInt(source.length)
      val r2 = Random.nextInt(source.length)
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

  def getNextGeneration(fa: Chromosome, mo: Chromosome): Chromosome = {
    val son = crossover(fa, mo)
    mutate(son)
  }

  def mutate(son: Chromosome): Chromosome = {
    val N = 10
    val newgene = son.genes.map.map(x=>{
      Random.nextInt(100) match {
        case i if i>3 =>x
        case _ =>{
          val pos = x._1.split("_")(1).toLowerCase
          val l = tagMap(pos).toList
          val r = Random.nextInt(l.length)
          (x._1,Champion(l(r),x._2.score))
        }
      }

    })
    val map = Champions(newgene)
    val fitness = Fitness.getCollocationDegreeByGenes(map)
    Chromosome(map,fitness)
  }

  def crossover(fa: Chromosome, mo: Chromosome): Chromosome = {
    val faGenes = fa.genes.map
    val maGenes = mo.genes.map
    val sonGenes = faGenes.map(x=>{
      val key = x._1
      val r = Random.nextInt(1)
      r match {
        case i if i ==0 =>(key, x._2)
        case _=> (key, maGenes (key) )
      }
    })
    val map = Champions(sonGenes)
    val fitness = Fitness.getCollocationDegreeByGenes(map)
    Chromosome(map,fitness)
  }
}
