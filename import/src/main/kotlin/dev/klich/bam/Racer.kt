package dev.klich.bam

import dev.klich.bam.repository.RaceInfoRepository
import java.lang.RuntimeException
import java.time.Duration


data class RaceId(val year: Int,
                  val index: Int)

object Races {
    val rybnik = RaceId(year = 2019,
            index = 0)

    val dabrowa = RaceId(year = 2019,
            index = 1)

    val ustron = RaceId(year = 2019,
            index = 2)

    val olkusz = RaceId(year = 2019, index = 3)
}

data class RaceInfo(val raceId: RaceId,
                    val bestHobbyResult: Duration,
                    val bestProResult: Duration,
                    val numberOfDnfs: Int) {
    fun bestResult(bamDistance: BamDistance): Duration {
        return if (bamDistance == BamDistance.HOBBY) {
            bestHobbyResult
        } else {
            bestProResult
        }
    }
}

typealias BibNumber = Int

data class Racer(val bibNumber: BibNumber,
                 val bamResults: List<BamResult>)


fun sector(racer: Racer, raceId: RaceId, raceInfoRepository: RaceInfoRepository): Int {
    return racer.bamResults.filter { it.raceId.index < raceId.index }
            .filter { !it.dnf }
            .takeLast(2)
            .map { raceInfoRepository.find(it.raceId)?.bestResult(it.bamDistance)?.let { bestResult -> computeSector(fastestBamResult = bestResult, bamResult = it) } ?: throw RuntimeException("Couldn't find race")}
            .min() ?: 7
}

enum class BamCategory {
    M16,
    M19,
    M30,
    M40,
    M50,
    M60,
    M70,
    M80,
    K16,
    K19,
    K30,
    K40,
    K50,
    K60,
    K70,
    K80,
    ELITE_K,
    ELITE_M
}