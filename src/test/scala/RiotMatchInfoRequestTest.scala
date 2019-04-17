
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{ Futures, ScalaFutures}
import org.scalatest._
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Seconds, Span}

class RiotMatchInfoRequestTest extends FlatSpec with Matchers with Futures with ScalaFutures with TryValues with Inside  {
  val config= ConfigFactory.load()
  val apikey = config.getString("riot.apikey ")
  val list:List[String] = List( "3019164405")
  val riotMatch = RiotMatchInfoRequest(list)
  "transJsonToRow" should "success " taggedAs Slow in{
    val json ="""{
                    "seasonId": 13,
                    "queueId": 420,
                    "gameId": 3019164405,
                    "gameVersion": "9.7.269.2391",
                    "platformId": "NA1",
                    "gameMode": "CLASSIC",
                    "mapId": 11,
                    "gameType": "MATCHED_GAME",
                    "teams": [
                        {
                            "firstDragon": false,
                            "firstInhibitor": true,
                            "win": "Win",
                            "firstRiftHerald": true,
                            "firstBaron": false,
                            "baronKills": 1,
                            "riftHeraldKills": 1,
                            "firstBlood": false,
                            "teamId": 100,
                            "firstTower": true,
                            "vilemawKills": 0,
                            "inhibitorKills": 4,
                            "towerKills": 11,
                            "dominionVictoryScore": 0,
                            "dragonKills": 2
                        },
                        {
                            "firstDragon": true,
                            "firstInhibitor": false,
                            "win": "Fail",
                            "firstRiftHerald": false,
                            "firstBaron": true,
                            "baronKills": 1,
                            "riftHeraldKills": 0,
                            "firstBlood": true,
                            "teamId": 200,
                            "firstTower": false,
                            "vilemawKills": 0,
                            "inhibitorKills": 0,
                            "towerKills": 1,
                            "dominionVictoryScore": 0,
                            "dragonKills": 1
                        }
                    ],
                    "participants": [
                        {
                
                            "spell1Id": 4,
                            "participantId": 1,
                            "highestAchievedSeasonTier": "GOLD",
                            "spell2Id": 14,
                            "teamId": 100,
                            "timeline": {
                                "lane": "BOTTOM",
                
                                "role": "DUO_SUPPORT"
                
                            },
                            "championId": 111
                        },
                        {
                
                            "spell1Id": 4,
                            "participantId": 2,
                            "highestAchievedSeasonTier": "GOLD",
                            "spell2Id": 7,
                            "teamId": 100,
                            "timeline": {
                                "lane": "BOTTOM",
                                "role": "DUO_CARRY"
                            },
                            "championId": 51
                        },
                        {
                            "spell1Id": 4,
                            "participantId": 3,
                            "highestAchievedSeasonTier": "SILVER",
                            "spell2Id": 7,
                            "teamId": 100,
                            "timeline": {
                                "lane": "TOP"
                            },
                            "championId": 67
                        },
                        {
                
                            "spell1Id": 6,
                            "participantId": 4,
                            "highestAchievedSeasonTier": "GOLD",
                            "spell2Id": 11,
                            "teamId": 100,
                            "timeline": {
                                "lane": "JUNGLE"
                            },
                            "championId": 141
                        },
                        {
                            "spell1Id": 21,
                            "participantId": 5,
                            "highestAchievedSeasonTier": "SILVER",
                            "spell2Id": 4,
                            "teamId": 100,
                            "timeline": {
                                "lane": "MIDDLE"
                            },
                            "championId": 101
                        },
                        {
                
                            "spell1Id": 4,
                            "participantId": 6,
                            "highestAchievedSeasonTier": "SILVER",
                            "spell2Id": 3,
                            "teamId": 200,
                            "timeline": {
                                "lane": "MIDDLE"
                            },
                            "championId": 74
                        },
                        {
                
                            "spell1Id": 14,
                            "participantId": 7,
                            "highestAchievedSeasonTier": "GOLD",
                            "spell2Id": 4,
                            "teamId": 200,
                            "timeline": {
                                "lane": "TOP"
                            },
                            "championId": 86
                        },
                        {
                
                            "spell1Id": 7,
                            "participantId": 8,
                            "highestAchievedSeasonTier": "SILVER",
                            "spell2Id": 4,
                            "teamId": 200,
                            "timeline":{
                                "lane": "BOTTOM",
                                "role":"DUO_SUPPORT"
                            },
                            "championId": 222
                        },
                        {
                
                            "spell1Id": 11,
                            "participantId": 9,
                            "highestAchievedSeasonTier": "GOLD",
                            "spell2Id": 4,
                            "teamId": 200,
                            "timeline": {
                                "lane": "JUNGLE"
                
                            },
                            "championId": 121
                        },
                        {
                
                            "spell1Id": 3,
                            "participantId": 10,
                            "highestAchievedSeasonTier": "SILVER",
                            "spell2Id": 4,
                            "teamId": 200,
                            "timeline": {
                                "lane": "BOTTOM",
                                "role": "DUO_SUPPORT"
                            },
                            "championId": 267
                        }
                    ]
                }"""
    val row = riotMatch.transJsonToRow(json)
    print(row)
    row should not be null
  }

  "test requestForMatchInfo" should "success" taggedAs Slow in{
    whenReady(riotMatch.requestForMatchInfo(),timeout(Span(10, Seconds))){w=>w.length should be >0 }
  }

}
