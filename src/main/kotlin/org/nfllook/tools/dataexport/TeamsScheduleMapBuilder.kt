package org.nfllook.tools.dataexport

import org.nfllook.tools.generated.wsch.Schedule
import java.util.*

enum class GameType { HOME, AWAY }

data class Opponent(val week: Int, val name: String, val gameType: GameType)

data class TeamSchedule(val games: MutableList<Opponent>)

object TeamsScheduleMapBuilder {

    fun build(yearSchedule: ArrayList<Schedule>): Map<String, TeamSchedule> {
        val teamsScheduleMap: MutableMap<String, TeamSchedule> = mutableMapOf()
        yearSchedule.forEach {
            val week = it.week
            it.games.forEach {
                val awayTeam = Opponent(week, it.away, GameType.AWAY)
                addWeekSchedule(teamsScheduleMap, it.home, awayTeam)
                val homeTeam = Opponent(week, it.home, GameType.HOME)
                addWeekSchedule(teamsScheduleMap, it.away, homeTeam)
            }
        }
        return teamsScheduleMap
    }

    private fun addWeekSchedule(teamsScheduleMap: MutableMap<String, TeamSchedule>, team: String, opponent: Opponent) {
        val teamSchedule = teamsScheduleMap[team]
        if (teamSchedule == null) {
            teamsScheduleMap.put(team, TeamSchedule(mutableListOf(opponent)))
        } else {
            teamSchedule.games.add(opponent)
        }
    }
}