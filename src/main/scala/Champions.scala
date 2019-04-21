case class Champion(championId:Int,score:Double){

}
case class Champions(map :Map[String,Champion]) {

}

case class Chromosome(genes:Champions,fitness: Int) {

}