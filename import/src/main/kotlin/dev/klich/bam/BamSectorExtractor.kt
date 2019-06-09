package dev.klich.bam

import dev.klich.bam.sector.Season
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula.ObjectExtractor
import technology.tabula.Table
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm
import technology.tabula.extractors.BasicExtractionAlgorithm
import technology.tabula.extractors.ExtractionAlgorithm
import java.io.InputStream
import java.math.BigDecimal
import java.nio.charset.Charset

class BamSectorExtractor

fun extractSectorTables(inputStream: InputStream): List<out Table>? {
    val extractionAlgorithm: ExtractionAlgorithm = BasicExtractionAlgorithm()
    val detectionAlgorithm = SpreadsheetDetectionAlgorithm()
    PDDocument.load(inputStream).use { doc ->
        val objectExtractor = ObjectExtractor(doc)
        return objectExtractor.extract().asSequence()
                .map { it.getArea(detectionAlgorithm.detect(it).first()) }
                .map { page ->
                    extractionAlgorithm.extract(page).toList()
                }
                .toList()
                .flatten()
    }
}

fun extractSectorRacerInformation(inputStream: InputStream): List<SectorRecord> {
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

fun List<String>.extractCoefficients(headers: List<String>): List<RaceCoefficient> {
    val extractYear = { str: String -> str.replace(Regex("[^0-9]"), "")}
    val raceIds = headers.subList(1, headers.size - 2)
            .groupBy(extractYear)
            .values
            .flatMap{ it.mapIndexed { index, s -> RaceId(index = index, year = Integer.parseInt(extractYear(s))) } }

    val coeffs = raceIds.zip(subList(1, headers.size - 2))
            .mapIndexedNotNull { index, pair ->
        try {
            RaceCoefficient(value = BigDecimal(pair.second.replace(",", ".")), raceId = pair.first)
        } catch (t: Throwable) {
            null
        }
    }

    return coeffs
}

data class RaceCoefficient(val value: BigDecimal, val raceId: RaceId)

data class SectorRecord(val name: String, val currentSector: String,
                        val maxCoefficient: BigDecimal, val coefficients: List<RaceCoefficient>)

data class SectorRacerInformation(val bibNumber: BibNumber,
                                  val season: Season,
                                  val name: String,
                                  val coefficients: List<RaceCoefficient>) {
    fun sector(raceId: RaceId): Int {
        return coefficients.takeWhile { it.raceId.year * 100 + it.raceId.index < raceId.year * 100 + raceId.

                index }
                .takeLast(3)
                .maxBy { it.value }
                ?.let {
                    sectorClassificator(it.value)
                } ?: 7
    }

    private fun sectorClassificator(coefficientValue: BigDecimal): Int {
        return when {
            coefficientValue > BigDecimal("0.87") -> 1
            coefficientValue > BigDecimal("0.82") -> 2
            coefficientValue > BigDecimal("0.78") -> 3
            coefficientValue > BigDecimal("0.74") -> 4
            coefficientValue > BigDecimal("0.69") -> 5
            coefficientValue > BigDecimal("0.59") -> 6
            else -> 7
        }
    }

}