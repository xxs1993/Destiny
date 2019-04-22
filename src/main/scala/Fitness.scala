import MLPart.WinRate
object Fitness {

  val win = WinRate()
  def getCollocationDegreeByGenes(champions: Champions): Double = {
    //TODO: call the method from model to get the win rate
    val map = champions.map
    val list = List(map("Win_TOP").championId,map("Win_JUG").championId,map("Win_MID").championId,map("Win_BOT").championId,
      map("Win_SUP").championId,map("Fail_TOP").championId,map("Fail_JUG").championId,map("Fail_MID").championId,map("Fail_BOT").championId,map("Fail_SUP").championId)
//    val list  = champions.map.values.toList.map(x=>x.championId)
    win.predictWinRate(list)*10000
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
