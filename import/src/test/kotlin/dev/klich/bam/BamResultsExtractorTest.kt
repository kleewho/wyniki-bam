package dev.klich.bam

import dev.klich.bam.repository.DynamoDBRaceInfoRepository
import dev.klich.bam.repository.RacerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import technology.tabula.writers.CSVWriter

class BamResultsExtractorTest {

    @Test
    fun test() {
        val result = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/rybnik.pdf"))
        println(result)
        val stringBuffer =  StringBuffer()
        CSVWriter().write(stringBuffer, result)
        println(stringBuffer)
    }

    @Test
    fun test2() {
        val result = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/rybnik.pdf"))!!
        val bamResults = result.flatMap { it.toBamResults(Races.rybnik) }
        println(bamResults.getFastestResult(BamDistance.HOBBY))
        println(bamResults.getFastestResult(BamDistance.PRO))
        println(bamResults.getFastestResult(BamDistance.FAMILY))
    }

    @Test
    fun test3() {
        val result = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/rybnik.pdf"))!!
        val bamResults = result.flatMap { it.toBamResults(Races.rybnik) }
        val fastestHobby = bamResults.getFastestResult(BamDistance.HOBBY)

        val me = bamResults.find { it.bibNumber == 1839}

        println(computeSector(fastestHobby.totalTime!!, me!!))
    }

    @Test
    fun test4() {
        val racesRepository = DynamoDBRaceInfoRepository.create()
        val racersRepository = RacerRepository()
        val rybnikTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/rybnik.pdf"))!!
        val bamRybnikResults = rybnikTables.flatMap { it.toBamResults(Races.rybnik) }
        val rybnikRaceInfo = RaceInfo(raceId = Races.rybnik, bestHobbyResult = bamRybnikResults.getFastestResult(BamDistance.HOBBY).totalTime!!,
                bestProResult = bamRybnikResults.getFastestResult(BamDistance.PRO).totalTime!!,
                numberOfDnfs = bamRybnikResults.count { it.dnf })
        racesRepository.store(rybnikRaceInfo)
        bamRybnikResults
                .filter { it.bamDistance == BamDistance.HOBBY || it.bamDistance == BamDistance.PRO || it.bamDistance == BamDistance.UNCLASSIFIED}
                .forEach {
                    val maybeRacer = racersRepository.find(it.bibNumber)
                    val updatedRacer = maybeRacer?.copy(bamResults = maybeRacer.bamResults + listOf(it)) ?: Racer(bibNumber = it.bibNumber,
                            bamResults = listOf(it))

                    racersRepository.store(updatedRacer)
                }


        val dabrowaTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/dabrowa.pdf"))!!
        val bamDabrowaResults = dabrowaTables.flatMap { it.toBamResults(Races.dabrowa) }
        val dabrowaRaceInfo = RaceInfo(raceId = Races.dabrowa, bestHobbyResult = bamDabrowaResults.getFastestResult(BamDistance.HOBBY).totalTime!!,
                bestProResult = bamDabrowaResults.getFastestResult(BamDistance.PRO).totalTime!!,
                numberOfDnfs = bamDabrowaResults.count { it.dnf })
        racesRepository.store(dabrowaRaceInfo)
        bamDabrowaResults
                .filter { it.bamDistance == BamDistance.HOBBY || it.bamDistance == BamDistance.PRO || it.bamDistance == BamDistance.UNCLASSIFIED }
                .forEach {
                    val maybeRacer = racersRepository.find(it.bibNumber)
                    val updatedRacer = maybeRacer?.copy(bamResults = maybeRacer.bamResults + listOf(it)) ?: Racer(bibNumber = it.bibNumber,
                            bamResults = listOf(it))

                    racersRepository.store(updatedRacer)
                }

        val ustronTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/ustron.pdf"))!!
        val bamUstronResults = ustronTables.flatMap { it.toBamResults(Races.ustron) }
        val ustronRaceInfo = RaceInfo(raceId = Races.ustron, bestHobbyResult = bamUstronResults.getFastestResult(BamDistance.HOBBY).totalTime!!,
                bestProResult = bamUstronResults.getFastestResult(BamDistance.PRO).totalTime!!,
                numberOfDnfs = bamUstronResults.count { it.dnf })
        racesRepository.store(ustronRaceInfo)
        bamUstronResults
                .filter { it.bamDistance == BamDistance.HOBBY || it.bamDistance == BamDistance.PRO || it.bamDistance == BamDistance.UNCLASSIFIED }
                .forEach {
                    val maybeRacer = racersRepository.find(it.bibNumber)
                    val updatedRacer = maybeRacer?.copy(bamResults = maybeRacer.bamResults + listOf(it)) ?: Racer(bibNumber = it.bibNumber,
                            bamResults = listOf(it))

                    racersRepository.store(updatedRacer)
                }

        val olkuszTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/olkusz.pdf"))!!
        val bamOlkuszResults = olkuszTables.flatMap { it.toBamResults(Races.ustron) }
        val olkuaszRaceInfo = RaceInfo(raceId = Races.ustron, bestHobbyResult = bamOlkuszResults.getFastestResult(BamDistance.HOBBY).totalTime!!,
                bestProResult = bamOlkuszResults.getFastestResult(BamDistance.PRO).totalTime!!,
                numberOfDnfs = bamOlkuszResults.count { it.dnf })
        racesRepository.store(olkuaszRaceInfo)
        val me = racersRepository.find(1839)

//        Reports.howManyInSectors(BamDistance.PRO, bamOlkuszResults, racersRepository, racesRepository)
//                .forEach { (k, v) ->
//                    println("There was $v ppl in sector $k riding PRO")
//                }
//
//        Reports.howManyInSectors(BamDistance.HOBBY, bamOlkuszResults, racersRepository, racesRepository)
//                .forEach { (k, v) ->
//                    println("There was $v ppl in sector $k riding HOBBY")
//                }
//
//        println("I started my race from sector ${sector(racer = me!!, raceId = Races.ustron, raceInfoRepository = racesRepository)}")
//
//        Reports.fasterThanBySectors(me.bibNumber, bamOlkuszResults, racersRepository, racesRepository)
//                .forEach { (k, v) ->
//                    println("I was faster than $v ppl from sector $k")
//                }
//
//        Reports.slowerThanBySectors(me.bibNumber, bamOlkuszResults, racersRepository, racesRepository)
//                .forEach { (k, v) ->
//                    println("I was slower than $v ppl from sector $k")
//                }

        println("Rybnik dnf percentage: ${Reports.dnfPercentange(bamRybnikResults)}%")
        println("Dabrowa dnf percentage: ${Reports.dnfPercentange(bamDabrowaResults)}%")
        println("Ustron dnf percentage: ${Reports.dnfPercentange(bamUstronResults)}%")
        println("Olkusz dnf percentage: ${Reports.dnfPercentange(bamOlkuszResults)}%")
    }

    @Test
    fun test5() {
        val ustronTables = extractTables(BamResultsExtractorTest::class.java.getResourceAsStream("/ustron.pdf"))!!
        val stringBuffer =  StringBuffer()
        CSVWriter().write(stringBuffer, ustronTables)
        println(stringBuffer)
        val bamUstronResults = ustronTables.flatMap { it.toBamResults(Races.ustron) }
        assertEquals(48, bamUstronResults.count { it.dnf })
    }



}