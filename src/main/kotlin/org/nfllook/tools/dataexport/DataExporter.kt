package org.nfllook.tools.dataexport

import org.nfllook.tools.generated.gd.Away
import org.nfllook.tools.generated.gd.Home
import org.nfllook.tools.generated.wst.Team
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*


class DataExporter(val path: String, val season: Int, val dataService: DataService) {

    enum class GameResult {HOME_WIN, AWAY_WIN, DRAW }

    var standings = dataService.getStandingsBootstrap()

    fun export() {
        val weekDirs = File("$path/$season").list()
        weekDirs.sortBy { it.toInt() }
        weekDirs.forEach {
            exportWeekStandings(it)
        }
    }

    private fun exportWeekStandings(it: String) {
        val week = it.toInt()
        processWeekData(week)
        pushWeekStandings(week)
    }

    private fun processWeekData(week: Int) {
        standings.week = week
        calcTeamsRecords(week)
        calcOppsTeamsRecords(week)
    }

    private fun calcTeamsRecords(week: Int) {
        val weekDir = "$path/$season/$week"
        File(weekDir).list({ dir, fileName -> (fileName.endsWith(".clean.json") && fileName != "schedule.clean.json") })
                .forEach { calcTeamsRecord("$weekDir/$it") }
    }

    private fun calcTeamsRecord(gameFile: String) {
        val gameData = dataService.getGameData(gameFile)
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
        homeTeam.winningPercentage = calcWinningPercentage(homeTeam.wins!!, homeTeam.losses!!)
        awayTeam.winningPercentage = calcWinningPercentage(awayTeam.wins!!, awayTeam.losses!!)
    }

    private fun calcWinningPercentage(w: Int, l: Int): Int {
        if(l == 0) {
            return 100
        } else {
            val wins = BigDecimal.valueOf(w.toDouble())
            val losses = BigDecimal.valueOf(l.toDouble())
            val res = wins.divide((wins.plus(losses)), 3, RoundingMode.HALF_DOWN).multiply(BigDecimal(100))
            return res.round(MathContext(0, RoundingMode.HALF_DOWN)).toInt()
        }
    }

    private fun calcOppsTeamsRecords(week: Int) {
        dataService.teamsScheduleMap.forEach {
            val team = findTeam(it.key)
            val teamSchedule = it.value
            team.oppsOfPlayedGames.wins = 0
            team.oppsOfPlayedGames.draws = 0
            team.oppsOfPlayedGames.losses = 0
            team.oppsOfRemainingGames.wins = 0
            team.oppsOfRemainingGames.draws = 0
            team.oppsOfRemainingGames.losses = 0
            teamSchedule.games.forEach {
                val opponent = findTeam(it.name)
                if(it.week <= week.toInt()) {
                    team.oppsOfPlayedGames.wins = team.oppsOfPlayedGames.wins + opponent.wins
                    team.oppsOfPlayedGames.draws = team.oppsOfPlayedGames.draws + opponent.draws
                    team.oppsOfPlayedGames.losses = team.oppsOfPlayedGames.losses + opponent.losses
                }
                if(it.week > week.toInt()) {
                    team.oppsOfRemainingGames.wins = team.oppsOfRemainingGames.wins + opponent.wins
                    team.oppsOfRemainingGames.draws = team.oppsOfRemainingGames.draws + opponent.draws
                    team.oppsOfRemainingGames.losses = team.oppsOfRemainingGames.losses + opponent.losses
                }
            }
            team.oppsOfPlayedGames.winningPercentage = calcWinningPercentage(team.oppsOfPlayedGames.wins!!, team.oppsOfPlayedGames.losses!!)
            team.oppsOfRemainingGames.winningPercentage = calcWinningPercentage(team.oppsOfRemainingGames.wins!!, team.oppsOfRemainingGames.losses!!)
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

    private fun pushWeekStandings(week: Int) {
        standings.id = season.toString() + "_" + week
        standings.uploadDate = Date().toString()
        dataService.pushStandings(standings)
    }
}
