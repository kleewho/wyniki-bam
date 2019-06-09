package dev.klich.bam

import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula.*
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm
import technology.tabula.extractors.BasicExtractionAlgorithm
import technology.tabula.extractors.ExtractionAlgorithm
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm
import java.io.InputStream
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeParseException

fun extractTables(inputStream: InputStream): List<out Table>? {
    val extractionAlgorithm: ExtractionAlgorithm = SpreadsheetExtractionAlgorithm()
    PDDocument.load(inputStream).use { doc ->
        val objectExtractor = ObjectExtractor(doc)
        return objectExtractor.extract().asSequence()
                .map(::getAreaOfInterest)
                .map { page ->
                    extractionAlgorithm.extract(page).toList()
                }
                .toList()
                .flatten()
    }
}



fun getAreaOfInterest(page: Page): Page {
    val otherPagesRectangle = Rectangle(80.48f, 17.89f, 804.86f, 482.91f)
    return page.getArea(otherPagesRectangle)
}

data class BamResult(val name: String,
                     val bibNumber: BibNumber,
                     val category: String,
                     val intermediateTimes: List<Duration>,
                     val raceId: RaceId,
                     val team: String?,
                     val placeOpen: Int?,
                     val placeOpenBySex: Int?,
                     val placeInCategory: Int?,
                     val bamDistance: BamDistance,
                     val dnf: Boolean) {
    val totalTime = intermediateTimes.lastOrNull()
}

fun Table.toBamResults(raceId: RaceId): List<BamResult> {
    return rows.mapNotNull { it.toBamResult(raceId) }
}

fun computeSector(fastestBamResult: Duration, bamResult: BamResult): Int? {
    if (bamResult.totalTime == null) {
        throw RuntimeException("Each results have to have totalTime set")
    }

    if (bamResult.category.contains("ELITA")) {
        return 1
    }

    return when (bamResult.bamDistance) {
        BamDistance.PRO -> sectorClassificator(fastestBamResult.seconds.toDouble() / bamResult.totalTime.seconds)

        BamDistance.HOBBY -> sectorClassificator(fastestBamResult.seconds.toDouble() / bamResult.totalTime.seconds * 0.94)
        BamDistance.FAMILY -> 8
        BamDistance.UNCLASSIFIED -> 7
    }
}

fun sectorClassificator(result: Double): Int {
    return when {
        result > 0.87 -> 1
        result > 0.82 -> 2
        result > 0.78 -> 3
        result > 0.74 -> 4
        result > 0.69 -> 5
        result > 0.59 -> 6
        else -> 7
    }
}

fun List<RectangularTextContainer<HasText>>.toBamResult(raceId: RaceId): BamResult? {
    val bamDistance = this[6].text.getBamDistance()
    val dnf = "DNF" == this[0].text
    return if (bamDistance == BamDistance.HOBBY || bamDistance == BamDistance.PRO || dnf) {
        BamResult(
                placeOpen = this[0].text.toIntOrNull(),
                placeInCategory = this[1].text.toIntOrNull(),
                placeOpenBySex = this[2].text.toIntOrNull(),
                name = this[3].text,
                team = this[4].text,
                bibNumber = this[5].text.toInt(),
                category = this[6].text,
                bamDistance = this[6].text.getBamDistance(),
                raceId = raceId,
                dnf = dnf,
                intermediateTimes = intermediateTimes())
    } else {
        null
    }
}

fun List<RectangularTextContainer<HasText>>.intermediateTimes(): List<Duration> {
    return listOfNotNull(this[7].toDuration(), this[8].toDuration(), this[9].toDuration(), this[10].toDuration())
}

fun RectangularTextContainer<HasText>.toDuration(): Duration? {
    return try {
        Duration.ofNanos(LocalTime.parse(text.replace(",", ".")).toNanoOfDay())
    } catch (ex: DateTimeParseException) {
        null
    }
}


fun List<BamResult>.getFastestResult(bamDistance: BamDistance): BamResult {
    return first { it.bamDistance == bamDistance }
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


