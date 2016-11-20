package org.nfllook.tools.dataexport

import com.evalab.core.cli.Command
import com.evalab.core.cli.exception.OptionException
import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo

fun main(args: Array<String>) {

    // --uri=mongodb://user:pass@host:28017/nfllookdb
    val command = Command("export", "Export nfllook data to MongoDB")
    command.addStringOption("uri", true, 'u', "MongoDB connection string")

    try {
        command.parse(args)
    } catch (ex: OptionException) {
        println(ex.message)
        println(command.getHelp())
        System.exit(2)
    }

    val uri = command.getStringValue("uri")

    val client = KMongo.createClient(MongoClientURI(uri))
    val database = client.getDatabase("nfllookdb")
    val collection = database.getCollection("testCollection")

    print(collection.count())
}