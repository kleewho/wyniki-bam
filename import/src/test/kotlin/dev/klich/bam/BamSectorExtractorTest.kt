package dev.klich.bam

import dev.klich.bam.sector.DynamoDBSectorRacerInformationRepository
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory


class BamSectorExtractorTest {
    val logger = LoggerFactory.getLogger(BamSectorExtractorTest::class.java)
    @Test
    fun test1() {
        val sectorRecords = extractSectorRacerInformation(BamSectorExtractorTest::class.java.getResourceAsStream("/sektory.csv"))!!
        //sectorRacerInfos.forEach(::println)
//        val stringBuffer =  StringBuffer()
//        CSVWriter().write(stringBuffer, listOf(sectorRacerInfos.first()))
//        println(stringBuffer)
        val olkuszTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/olkusz.pdf"))!!
        val bamOlkuszResults = olkuszTables.flatMap { it.toBamResults(Races.ustron) }
        val me = bamOlkuszResults.find {
            it.bibNumber == 1839
        }!!

        val ustronTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/ustron.pdf"))!!
        val bamUstronResults = ustronTables.flatMap { it.toBamResults(Races.ustron) }
        val meUstron = bamUstronResults.find {
            it.bibNumber == 1839
        }!!

        val resultsByName = bamUstronResults.groupBy { it.name }

        val sectorRacerInfors = sectorRecords.mapNotNull{ sectorRecord ->
            resultsByName[sectorRecord.name]?.let {
                val matchingBamResults = it.filter {
                    sectorRecord.coefficients.find { raceCoefficient -> it.raceId == raceCoefficient.raceId } != null
                }

                if (matchingBamResults.size == 1) {
                    val matchingBamResult = matchingBamResults.first()
                    SectorRacerInformation(season = matchingBamResult.raceId.year, name = sectorRecord.name,
                            coefficients = sectorRecord.coefficients, bibNumber = matchingBamResult.bibNumber)
                } else {
                    logger.warn("Ignoring sectorRecord $sectorRecord")
                    null
                }
            }
        }

        val sectorRepository = DynamoDBSectorRacerInformationRepository.create()

        sectorRacerInfors.forEach {
            //sectorRepository.store(it)
        }

        val meFromRepo = sectorRepository.find(2019, 1839)

        val result = sectorRecords
                .filter { it.name == me.name }
                .filter { it.coefficients.find { raceCoefficient -> Races.ustron == raceCoefficient.raceId } != null }
                .map {
                    SectorRacerInformation(bibNumber = me.bibNumber,
                            name = it.name,
                            coefficients = it.coefficients,
                            season = me.raceId.year)
                }

        sectorRecords.filter {
            it.name == me.name
        }

        println(result)
        println("Starting brenna in sector ${result.first().sector(RaceId(year = 2018, index = 2))}")
        println("Starting olkusz in sector ${result.first().sector(RaceId(year = 2018, index = 3))}")
        println("Starting zarki in sector ${result.first().sector(RaceId(year = 2018, index = 4))}")
        println("Starting rybnik in sector ${result.first().sector(Races.rybnik)}")
        println("Starting dabrowa in sector ${result.first().sector(Races.dabrowa)}")
        println("Starting ustron in sector ${result.first().sector(Races.ustron)}")
        println("Starting olkusz in sector ${result.first().sector(Races.olkusz)}")


    }

}