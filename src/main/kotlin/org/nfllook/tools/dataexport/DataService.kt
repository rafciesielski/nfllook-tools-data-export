package org.nfllook.tools.dataexport

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.litote.kmongo.replaceOneById
import org.nfllook.tools.generated.gd.GameData
import org.nfllook.tools.generated.wsch.Schedule
import org.nfllook.tools.generated.wst.Standings
import java.io.File
import java.util.*

class DataService(uri: String) {

    val mapper = ObjectMapper()
    val database: MongoDatabase
    val scheduleCollection: MongoCollection<Schedule>
    val standingsCollection: MongoCollection<Standings>
    val teamsScheduleMap: Map<String, TeamSchedule>

    init {
        val client = KMongo.createClient(MongoClientURI(uri))
        database = client.getDatabase("nfllookdb")
        standingsCollection = database.getCollection<Standings>()
        scheduleCollection = database.getCollection<Schedule>()
        val yearSchedule = scheduleCollection.find().into(ArrayList<Schedule>())
        teamsScheduleMap = TeamsScheduleMapBuilder.build(yearSchedule)
    }

    fun getStandingsBootstrap(): Standings {
        val url = ClassLoader.getSystemClassLoader().getResource("week_standings_bootstrap.json")
        return mapper.readValue<Standings>(url, Standings::class.java)
    }

    fun getGameData(gameFile: String): GameData {
        return mapper.readValue<GameData>(File(gameFile), GameData::class.java)
    }

    fun pushStandings(standings: Standings) {
        standingsCollection.replaceOneById(standings.id, standings, UpdateOptions().upsert(true))
    }
}