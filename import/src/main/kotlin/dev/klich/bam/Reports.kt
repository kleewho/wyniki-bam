package dev.klich.bam

import dev.klich.bam.repository.RacerRepository
import dev.klich.bam.repository.RaceInfoRepository
import java.math.BigDecimal
import java.math.RoundingMode

object Reports {
    fun dnfPercentange(bamResults: List<BamResult>): BigDecimal {
        val riders = bamResults.filter { it.bamDistance == BamDistance.HOBBY || it.bamDistance == BamDistance.PRO || it.dnf }
        return BigDecimal.valueOf(riders.filter { it.dnf }.size.toDouble() / riders.size * 100).setScale(1, RoundingMode.HALF_EVEN)
    }

    fun fasterThanBySectors(bibNumber: BibNumber, bamResults: List<BamResult>, racerRepository: RacerRepository, raceInfoRepository: RaceInfoRepository): Map<Int, Int> {
        val racer = racerRepository.find(bibNumber)!!

        return bamResults
                .filter { racer.bamResults.last().bamDistance == it.bamDistance }
                .filter { it.totalTime!! > racer.bamResults.last().totalTime }
                .groupBy({ sector(racer = racerRepository.find(it.bibNumber)!!, raceId = Races.ustron, raceInfoRepository = raceInfoRepository)}, { it })
                .entries
                .sortedBy { it.key }
                .map { it.key to it.value.size }
                .toMap()
    }

    fun slowerThanBySectors(bibNumber: BibNumber, bamResults: List<BamResult>, racerRepository: RacerRepository, raceInfoRepository: RaceInfoRepository): Map<Int, Int> {
        val racer = racerRepository.find(bibNumber)!!

        return bamResults
                .filter { racer.bamResults.last().bamDistance == it.bamDistance }
                .filter { it.totalTime!! < racer.bamResults.last().totalTime }
                .groupBy({ sector(racer = racerRepository.find(it.bibNumber)!!, raceId = Races.ustron, raceInfoRepository = raceInfoRepository)}, { it })
                .entries
                .sortedBy { it.key }
                .map { it.key to it.value.size }
                .toMap()
    }

    fun howManyInSectors(bamDistance: BamDistance, bamResults: List<BamResult>, racerRepository: RacerRepository, raceInfoRepository: RaceInfoRepository): Map<Int, Int> {
        return bamResults
                .filter { bamDistance == it.bamDistance }
                .groupBy({ sector(racer = racerRepository.find(it.bibNumber)!!, raceId = Races.ustron, raceInfoRepository = raceInfoRepository)}, { it })
                .entries
                .sortedBy { it.key }
                .map { it.key to it.value.size }
                .toMap()
    }
}