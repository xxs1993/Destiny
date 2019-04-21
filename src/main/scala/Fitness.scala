import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
object Fitness {

  def getCollocationDegreeByGenes(champions: Champions): Int = { //TODO: call the method from model to get the win rate
    0
  }

  def fitness(list: List[Chromosome]): List[Chromosome] = { //TODO: select parent chromosome
    var collocationDegreeSum:Double = 0
    val range:List[Double] = list.map(x => {
      collocationDegreeSum = collocationDegreeSum + x.fitness
      collocationDegreeSum
    })
    val r1 = collocationDegreeSum * Math.random
    val r2 = collocationDegreeSum * Math.random
    val index1 = getSelectedIndex(range, r1)
    val index2 = getSelectedIndex(range, r2)
    List(list(index1), list(index2))
  }

  def getSelectedIndex(range: List[Double], r: Double): Int = {
    return range.filter(x => x < r).length
  }
}
