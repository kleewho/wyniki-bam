package dev.klich.bam.repository

import dev.klich.bam.BibNumber
import dev.klich.bam.Racer

class RacerRepository {
    private val racersStorage = mutableMapOf<BibNumber, Racer>()

    fun find(bibNumber: BibNumber): Racer? {
        return racersStorage[bibNumber]
    }

    fun store(racer: Racer) {
        racersStorage[racer.bibNumber] = racer
    }
}
