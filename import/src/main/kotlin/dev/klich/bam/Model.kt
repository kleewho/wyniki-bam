package dev.klich.bam

import java.time.Duration
import java.util.*

data class BasicRaceInformation(val city: String,
                                val index: Int,
                                val date: Date,
                                val year: Int)

typealias BibNumber = Int

sealed class Result {
    abstract val bibNumber: BibNumber
    abstract val name: String
}

data class BamResult(override val name: String,
                     override val bibNumber: BibNumber,
                     val category: String,
                     val intermediateTimes: List<Duration>,
                     val team: String?,
                     val placeOpen: Int?,
                     val placeOpenBySex: Int?,
                     val placeInCategory: Int?,
                     val bamDistance: BamDistance) : Result() {
    val totalTime = intermediateTimes.lastOrNull()
}

data class DnfResult(override val name: String,
                     override val bibNumber: BibNumber,
                     val category: String,
                     val intermediateTimes: List<Duration>,
                     val team: String?,
                     val bamDistance: BamDistance?) : Result()

data class DnsResult(override val name: String,
                     override val bibNumber: BibNumber,
                     val category: String,
                     val team: String?) : Result()

