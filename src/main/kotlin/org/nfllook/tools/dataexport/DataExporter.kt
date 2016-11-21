package org.nfllook.tools.dataexport

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.nfllook.tools.generated.ws.Standings
import java.io.File


class DataExporter(val path: String, val season: Int, val uri: String) {

    val mapper = ObjectMapper()
    val collection: MongoCollection<Standings>
    var standings: Standings

    init {
        val url = ClassLoader.getSystemClassLoader().getResource("week_standings_bootstrap.json")
        standings = mapper.readValue<Standings>(url, Standings::class.java)

        val client = KMongo.createClient(MongoClientURI(uri))
        val database = client.getDatabase("nfllookdb")
        collection = database.getCollection<Standings>()
        collection.drop()
    }

    fun export() {
        File("$path/$season").list().forEach {
            processOneWeekData("$path/$season/$it")
            pushWeekStandings(season, it, standings)
        }
    }

    private fun processOneWeekData(weekDir: String) {
        File(weekDir).list({ dir, fileName -> fileName.endsWith(".clean.json") })
                .forEach { processOneGameData("$weekDir/$it") }
    }

    private fun processOneGameData(gameFile: String) {
        /*val gameData = mapper.readValue<GameData>(File(gameFile), GameData::class.java)

        val gdHome = gameData.home
        val gdAway = gameData.away

        var teamHome = Team()
        teamHome.withName(gdHome.abbr)

        var teamAway = Team()
        teamAway.withName(gdAway.abbr)*/
    }

    private fun pushWeekStandings(season:Int, week:String, standing: Standings) {
        standings.id = season.toString() + "_" + week
        collection.insertOne(standing)
    }
}
