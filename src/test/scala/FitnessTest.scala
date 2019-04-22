import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class FitnessTest extends FlatSpec with Matchers with BeforeAndAfter{

  val spark = SparkSession
    .builder()
    .master("local")
    .appName("FitnessTest")
    .getOrCreate()
  var cham1:Champions = null
  var cham2:Champions = null
  before{
    val map1 = Map("Win_TOP"->6,"Win_JUG"->2,"Win_MID"->1,"Win_BOT"->15,"Win_SUP"->16,
      "Fail_TOP"->23,"Fail_JUG"->11,"Fail_MID"->13,"Fail_BOT"->22,"Fail_SUP"->25)
    val map2 = Map("Win_TOP"->516,"Win_JUG"->427,"Win_MID"->142,"Win_BOT"->145,"Win_SUP"->555,
      "Fail_TOP"->86,"Fail_JUG"->120,"Fail_MID"->268,"Fail_BOT"->119,"Fail_SUP"->412)
    cham1 = Champions(map1.map(x=>(x._1,Champion(x._2,10000))))
    cham2 = Champions(map2.map(x=>(x._1,Champion(x._2,10000))))
  }
  behavior of "FitnessTest"

  it should "getCollocationDegreeByGenes" in {
    val f1 = Fitness.getCollocationDegreeByGenes(cham1)
    val f11 = Fitness.getCollocationDegreeByGenes(cham1)
    val f2 = Fitness.getCollocationDegreeByGenes(cham2)
    f1==f11 shouldBe true
    f1==f2 shouldBe false

  }

  it should "fitness" in {
    val f1 = Fitness.getCollocationDegreeByGenes(cham1)
    val f2 = Fitness.getCollocationDegreeByGenes(cham2)
    val list= Fitness.fitness(List(Chromosome(cham1,f1),Chromosome(cham2,f2),Chromosome(cham1,f1),Chromosome(cham2,f2)))
    list.length shouldBe 2

  }



}
