import MLPart.ChampionTag

object ChampionPick {
  val map = ChampionTag().champ_position_map
  def pickChampionForPos(pos:String):List[Int]={
    ChampionTag().getTagByPosition(pos.toLowerCase()).toList
  }
}
