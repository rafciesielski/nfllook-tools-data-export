package org.nfllook.tools.dataexport

import com.evalab.core.cli.Command
import com.evalab.core.cli.exception.OptionException

private val PATH = "path"
private val SEASON = "season"
private val URI = "uri"

fun main(args: Array<String>) {

    val command = Command("export", "Export nfllook data to MongoDB")
    command.addStringOption(PATH, true, 'p', "Sets path to data directory")
    command.addIntegerOption(SEASON, true, 's', "Sets season")
    command.addStringOption(URI, true, 'u', "MongoDB connection string")

    try {
        command.parse(args)
    } catch (ex: OptionException) {
        println(ex.message)
        println(command.getHelp())
        System.exit(2)
    }

    val path = command.getStringValue(PATH)
    val season = command.getIntegerValue(SEASON)
    val uri = command.getStringValue(URI)

    println("Path: $path season: $season, uri: ${uri!!.replace(Regex("//.*?@"), "//<user>:<pass>@")}")

    DataExporter(path!!, season!!, DataService(uri)).export()
}