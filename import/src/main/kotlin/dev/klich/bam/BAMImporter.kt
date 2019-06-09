package dev.klich.bam

import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object BAMImporter {
    val logger = LoggerFactory.getLogger(BAMImporter::class.java)
}

object CmdOpts {
    const val results = "results"
    const val file = "file"
    const val helpLong = "help"
    const val helpShort = "h"
}

fun main(args: Array<String>) {
    val options = Options().apply {
        addOption(Option(CmdOpts.helpShort, CmdOpts.helpLong, false, "Print this message and exit"))
        addOption(CmdOpts.results, false, "Import new results")
        addOption(CmdOpts.file, true, "Path to file")
    }

    val cmd = DefaultParser().safeParse(options, args)

    BAMImporter.logger.info("Starting ${BAMImporter::class.java.simpleName} with command line args ${cmd.argList}")

    if (cmd.hasOption(CmdOpts.results)) {
        importResults(cmd.getOptionValue(CmdOpts.file))
    }
}

fun CommandLineParser.safeParse(options: Options, arguments: Array<String>): CommandLine {
    val cmd = try {
        parse(options, arguments)
    } catch (ex: UnrecognizedOptionException) {
        printHelp(options)
        exitProcess(1)
    }

    if (cmd.hasOption(CmdOpts.helpLong) || cmd.hasOption(CmdOpts.helpShort)) {
        printHelp(options)
        exitProcess(0)
    }
    return cmd
}

fun printHelp(options: Options) {
    HelpFormatter().printHelp("BAMImporter", options)
}
