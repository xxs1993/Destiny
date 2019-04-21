import javax.annotation.MatchesPattern
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class GARunTest extends FlatSpec with Matchers with BeforeAndAfter{

  var run:GARun = null
  before{
    val map1 = Map("Win_TOP"->6,"Win_JUG"->2,"Win_MID"->1,"Win_BOT"->15,"Win_SUP"->16,
      "Fail_TOP"->23,"Fail_JUG"->11,"Fail_MID"->13,"Fail_BOT"->22,"Fail_SUP"->25)
    val champion = Champions(map1.map(x=>(x._1,Champion(x._2,10000))))
    run = GARun(champion)
  }
  behavior of "GARunTest"

  it should "mature" in {
    run.mature(run.map)("mature").length should be >0
  }

  it should "getSolutions" in {
    run.getSolutions.length shouldBe GAConfiguration.sample_number
  }

  it should "survive" in {
    run.survive(run.map("mature")).length shouldBe GAConfiguration.sample_number/2
  }

  it should "breed" in {
    val matureLength = run.map("mature").length
    (run.breed(run.map)("immature").length-matureLength) should be >=0
  }

  it should "breedAndSelection" in {
    run.breedAndSelection(run.map)("immature").length shouldBe run.map("mature").length
  }

}
