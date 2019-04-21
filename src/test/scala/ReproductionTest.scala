import org.scalatest.{BeforeAndAfter, FlatSpec, FunSuite, Matchers}

class ReproductionTest extends FlatSpec with Matchers with BeforeAndAfter{

  var mo:Chromosome = null
  var fa:Chromosome = null
  before{
    val map1 = Map("Win_TOP"->6,"Win_JUG"->2,"Win_MID"->1,"Win_BOT"->15,"Win_SUP"->16,
      "Fail_TOP"->23,"Fail_JUG"->11,"Fail_MID"->13,"Fail_BOT"->22,"Fail_SUP"->25)
    val map2 = Map("Win_TOP"->516,"Win_JUG"->427,"Win_MID"->142,"Win_BOT"->145,"Win_SUP"->555,
      "Fail_TOP"->86,"Fail_JUG"->120,"Fail_MID"->268,"Fail_BOT"->119,"Fail_SUP"->412)
    mo = Chromosome(Champions(map1.map(x=>{
      (x._1,Champion(x._2,10000))
    })),0)
    fa = Chromosome(Champions(map2.map(x=>{
      (x._1,Champion(x._2,10000))
    })),0)
  }

  behavior of "Reproduction"
  it should  ("testMutate") in {
    val son = Reproduction.mutate(Reproduction.crossover(mo,fa))
    son should not be  null
    son.genes.map.size shouldBe 10
  }

  it should ("testCrossover") in{
    val son = Reproduction.crossover(mo,fa)
    son should not be  null
    son.genes.map.size shouldBe 10

  }

  it should ("testGetNextGeneration") in{
    val list = Reproduction.getNextGeneration(List(mo,fa))
    list.size shouldBe 2

  }

  it should ("testPick") in{
    val list = Reproduction.pick(List(mo,fa))
    list.size shouldBe 2
  }

}
