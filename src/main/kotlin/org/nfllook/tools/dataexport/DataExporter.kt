package org.nfllook.tools.dataexport

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.nfllook.tools.generated.gd.Away
import org.nfllook.tools.generated.gd.GameData
import org.nfllook.tools.generated.gd.Home
import org.nfllook.tools.generated.wst.Standings
import org.nfllook.tools.generated.wst.Team
import java.io.File


class DataExporter(val path: String, val season: Int, uri: String) {

    enum class GameResult {HOME_WIN, AWAY_WIN, DRAW }

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
        val weekDirs = File("$path/$season").list()
        weekDirs.sortBy { it.toInt() }
        weekDirs.forEach {
            processOneWeekData("$path/$season/$it")
            pushWeekStandings(season, it, standings)
        }
    }

    private fun processOneWeekData(weekDir: String) {
        standings.week++
        File(weekDir).list({ dir, fileName -> (fileName.endsWith(".clean.json") && fileName != "schedule.clean.json") })
                .forEach { processOneGameData("$weekDir/$it") }
    }

    private fun processOneGameData(gameFile: String) {

        val gameData = mapper.readValue<GameData>(File(gameFile), GameData::class.java)

        val homeTeam = findTeam(gameData.home.abbr)
        val awayTeam = findTeam(gameData.away.abbr)

        val gameResult = getGameResult(gameData.home, gameData.away)
        if (gameResult == GameResult.HOME_WIN) {
            homeTeam.wins++
            awayTeam.losses++
        } else if (gameResult == GameResult.AWAY_WIN) {
            homeTeam.losses++
            awayTeam.wins++
        } else {
            homeTeam.draws++
            awayTeam.draws++
        }
    }

    private fun getGameResult(home: Home, away: Away): GameResult {
        val homeScore = home.score.t
        val awayScore = away.score.t
        if (homeScore > awayScore) {
            return GameResult.HOME_WIN
        } else if (homeScore < awayScore) {
            return GameResult.AWAY_WIN
        } else {
            return GameResult.DRAW
        }
    }

    private fun findTeam(abbr: String): Team {
        standings.conferences.forEach {
            it.divisions.forEach {
                it.teams.forEach {
                    if (it.name == abbr) {
                        return it
                    }
                }
            }
        }
        throw Exception("Could not find team: $abbr")
    }

    private fun pushWeekStandings(season: Int, week: String, standing: Standings) {
        standings.id = season.toString() + "_" + week
        collection.insertOne(standing)
    }
}
