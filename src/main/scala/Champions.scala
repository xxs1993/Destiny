case class Champion(championId:Int,score:Double)extends Serializable{

}
case class Champions(map :Map[String,Champion]) extends Serializable{

}

case class Chromosome(genes:Champions,fitness: Double) extends Serializable{

}