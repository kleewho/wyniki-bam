package dev.klich.bam

import dev.klich.bam.BAMResultsInterpreter.logger
import dev.klich.bam.repository.DynamoDBRaceInfoRepository
import dev.klich.bam.repository.RaceId
import dev.klich.bam.repository.RaceInfo
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.math.BigDecimal
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeParseException

object BAMResultsInterpreter {
    val logger = LoggerFactory.getLogger(BAMResultsInterpreter::class.java)
}


fun importResults(path: String) {
    val filepath = Paths.get(path)
    val basicRaceInformation = extractBasicRaceInformation(filepath.fileName.toFile().name)

    Files.newInputStream(filepath).use { inputStream ->
        val format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()

        val results = CSVParser.parse(inputStream, Charset.defaultCharset(), format).use { parser ->
            parser.asSequence()
                    .map { it.toBamResult() }
                    .toList()
        }

        logger.info("Found ${results.size} results")

        val raceInfoRepository = DynamoDBRaceInfoRepository.create()
        val raceInfo = RaceInfo(raceId = RaceId(year = basicRaceInformation.year, index = basicRaceInformation.index),
                city = basicRaceInformation.city,
                totalNumberOfRacers = results.count { it !is DnsResult },
                totalNumberOfDnss = results.count { it is DnsResult },
                totalNumberOfDnfs = results.count { it is DnfResult },
                racers = listOf(),
                date = basicRaceInformation.date,
                year = basicRaceInformation.year)

        raceInfoRepository.store(raceInfo)
    }
}

fun CSVRecord.toBamResult() : Result {
    val dnf = "DNF" == this[0]
    val dns = "DNS" == this[0]
    val name = this[3]
    val team = this[4]
    val bibNumber = this[5].toInt()
    val category = this[6]

    return when {
        dns -> DnsResult(name = name,
                team = team,
                bibNumber = bibNumber,
                category = category)
        dnf -> DnfResult(name = name,
                team = team,
                bibNumber = bibNumber,
                category = category,
                intermediateTimes = intermediateTimes(),
                bamDistance = this[6].getBamDistance())
        else -> {
            BamResult(
                    placeOpen = this[0].toIntOrNull(),
                    placeInCategory = this[1].toIntOrNull(),
                    placeOpenBySex = this[2].toIntOrNull(),
                    name = this[3],
                    team = this[4],
                    bibNumber = this[5].toInt(),
                    category = this[6],
                    bamDistance = this[6].getBamDistance(),
                    intermediateTimes = intermediateTimes())

        }
    }
}

enum class BamDistance {
    PRO,
    HOBBY,
    FAMILY,
    UNCLASSIFIED
}

fun String.getBamDistance(): BamDistance {
    return when {
        contains("Pro") -> BamDistance.PRO
        contains("Hobby") -> BamDistance.HOBBY
        contains("Family") -> BamDistance.FAMILY
        else -> BamDistance.UNCLASSIFIED
    }
}


fun CSVRecord.intermediateTimes(): List<Duration> {
    return listOfNotNull(this[7].toDuration(), this[8].toDuration(), this[9].toDuration(), this[10].toDuration())
}

fun String.toDuration(): Duration? {
    return try {
        Duration.ofNanos(LocalTime.parse(replace(",", ".")).toNanoOfDay())
    } catch (ex: DateTimeParseException) {
        null
    }
}




fun extractBasicRaceInformation(name: String): BasicRaceInformation {
    val (city, index, year, date) = name.dropLast(4).split(":")
    return BasicRaceInformation(city = city,
            index = index.toInt(),
            date = SimpleDateFormat("yyyy-MM-dd").parse(date),
            year = year.toInt())
}

fun extractSectorRacerInformation2(inputStream: InputStream): List<SectorRecord> {
    val format = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()

    CSVParser.parse(inputStream, Charset.defaultCharset(), format).use { parser ->
        return parser.asSequence()
                .map {
                    val recordAsList = it.toList()
                    val name = recordAsList.first()
                    val sector = recordAsList.last()
                    val maxCoefficient = BigDecimal(recordAsList.dropLast(1).last().replace(",", "."))
                    val coefficients = recordAsList.extractCoefficients(parser.headerMap.keys.toList())
                    SectorRecord(name = name, currentSector = sector,
                            maxCoefficient = maxCoefficient, coefficients = coefficients)
                }.toList()
    }

}
