package svcs

import java.io.File
import kotlin.system.exitProcess

enum class Command(val description: String) {
    CONFIG("Get and set a username."),
    ADD("Add a file to the index."),
    LOG("Show commit logs."),
    COMMIT("Save changes."),
    CHECKOUT("Restore a file."),
    HELP("These are SVCS commands:\n" +
            "config     ${CONFIG.description}\n" +
            "add        ${ADD.description}\n" +
            "log        ${LOG.description}\n" +
            "commit     ${COMMIT.description}\n" +
            "checkout   ${CHECKOUT.description}"
    )
}

class VersionControl(args: Array<String>) {
    private val vcs = File("vcs")
    private val commits = File("vcs/commits")
    private val config = File("vcs/config.txt")
    private val index = File("vcs/index.txt")
    private val log = File("vcs/log.txt")

    init {
        vcs.mkdir()
        commits.mkdir()
        if (!config.exists()) config.writeText("")
        if (!index.exists()) index.writeText("")
        if (!log.exists()) log.writeText("")

        if (args.isEmpty() || args.first() == "--help") {
            println(Command.HELP.description)
            exitProcess(1)
        } else if (!Command.values().toList().any { it.toString() == args.first().uppercase() }) {
            println("'${args.first()}' is not a SVCS command.")
            exitProcess(2)
        }

        when(args.first()) {
            "config" -> config(args)
            "add" -> add(args)
            "log" -> log()
            "commit" -> commit(args)
            "checkout" -> checkout(args)
        }
    }

    private fun config(args: Array<String>) {
        if (args.count() == 1) {
            if (config.length() == 0L) {
                println("Please, tell me who you are.")
            } else println("The username is ${config.readText()}.")
        } else if (args.count() == 2) {
            config.writeText(args.last().toString())
            println("The username is ${config.readText()}.")
        }
    }

    private fun add(args: Array<String>) {
        val newStr = args.last()
        val newFile = File(newStr)

        if (args.count() == 1) {
            if (index.length() == 0L) {
                println("Add a file to the index.\n")
            } else println("Tracked files:\n${index.readText()}")
        } else if (args.count() == 2 ) {
            if (!newFile.exists()) {
                println("Can't find '$newStr'.")
            } else {
                index.appendText(newStr + "\n")
                println("The file '$newStr' is tracked.")
            }
        }
    }

    private fun commit(args: Array<String>) {
        var newCommit = ""
        for (i in 0..index.readLines().lastIndex) newCommit += File(index.readLines()[i]).readText()
        val newCommitHash = hashString(newCommit)
        val newCommitDir = File("vcs/commits/$newCommitHash")

        if (args.count() == 2 ) {
            if (newCommitDir.exists()) {
                println("Nothing to commit.")
            } else {
                val logStr = log.readText()
                log.writeText(
                    "commit $newCommitHash\n" +
                            "Author: ${config.readText()}\n" +
                            "${args.last()}\n$logStr"
                )
                newCommitDir.mkdir()
                for (i in 0..index.readLines().lastIndex) {
                    File(index.readLines()[i]).copyTo(File(newCommitDir, index.readLines()[i]))
                }
                println("Changes are committed.")
            }
        } else println("Message was not passed.")
    }

    private fun log() = if (log.length() == 0L) println("No commits yet.") else {
        for (str in log.readLines()) println(str)
    }

    private fun checkout(args: Array<String>) {
        if (args.count() == 2) {
            val commitHash = args.last()
            val commitsDirs = commits.listFiles()?.filter { it.isDirectory }

            commitsDirs?.map { if (commitHash == it.name) {
                for (i in 0..index.readLines().lastIndex) {
                    File(index.readLines()[i]).writeText(File("vcs/commits/$commitHash/${index.readLines()[i]}").readText())
                }
                println("Switched to commit $commitHash.")
            } else if (it.name == commitsDirs[commitsDirs.lastIndex].name) {
                println("Commit does not exist.")
            }
            }
        } else println("Commit id was not passed.")
    }

    private fun hashString(input: String): String {
        val charArray = input.toCharArray()
        val p = 2
        var result = 0

        for (ch in charArray) {
            result = result * p + ch.code
        }

        return result.toString().replaceFirstChar { '-' }
    }
}

fun main(args: Array<String>) {
    VersionControl(args)
}