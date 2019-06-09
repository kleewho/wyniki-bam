package dev.klich.bam.repository

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import dev.klich.bam.BibNumber
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

data class RaceInfo(val raceId: RaceId,
                    val totalNumberOfRacers: Int,
                    val racers: List<BasicRacerInformation>,
                    val totalNumberOfDnfs: Int,
                    val totalNumberOfDnss: Int,
                    val city: String,
                    val date: Date,
                    val year: Int)

data class BasicRacerInformation(val bibNumber: BibNumber,
                                 val category: String,
                                 val distance: String)

data class RaceId(val year: Int,
                  val index: Int)


interface RaceInfoRepository {
    fun find(raceId: RaceId): RaceInfo?
    fun store(raceInfo: RaceInfo)
}

class InMemoryRaceInfoRepository : RaceInfoRepository {
    private val racersStorage = mutableMapOf<RaceId, RaceInfo>()

    override fun find(raceId: RaceId): RaceInfo? {
        return racersStorage[raceId]
    }

    override fun store(raceInfo: RaceInfo) {
        racersStorage[raceInfo.raceId] = raceInfo
    }
}

class DynamoDBRaceInfoRepository(private val dynamoDB: DynamoDB) : RaceInfoRepository {
    val logger = LoggerFactory.getLogger(DynamoDBRaceInfoRepository::class.java)

    private val raceInfoTableName = "wyniki-bam-races"
    private val inMemoryRaceInfoRepository = InMemoryRaceInfoRepository()

    companion object {
        fun create(): DynamoDBRaceInfoRepository {
            val dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("dynamodb.eu-central-1.amazonaws.com", "eu-central-1"))
                    .build()
            val dynamoDB = DynamoDB(dynamoDBClient)
            return DynamoDBRaceInfoRepository(dynamoDB = dynamoDB)
        }

    }

    override fun find(raceId: RaceId): RaceInfo? {
        val table = dynamoDB.getTable(raceInfoTableName)
        return inMemoryRaceInfoRepository.find(raceId) ?:
        table.getItem("raceId", raceId.toHashKeyValue())?.let { item ->

            RaceInfo(raceId = raceId,
                    totalNumberOfDnfs = item.getInt(RaceInfo::totalNumberOfDnfs.name),
                    racers = listOf(),
                    totalNumberOfDnss = item.getInt(RaceInfo::totalNumberOfDnss.name),
                    totalNumberOfRacers = item.getInt(RaceInfo::totalNumberOfRacers.name),
                    city = item.getString(RaceInfo::city.name),
                    date = SimpleDateFormat("yyyy-MM-dd").parse(item.getString(RaceInfo::date.name)),
                    year = item.getInt(RaceInfo::year.name))
                    .also { inMemoryRaceInfoRepository.store(it) }
        }

    }

    override fun store(raceInfo: RaceInfo) {
        logger.info("Storing raceInfo for ${raceInfo.raceId}")
        val item = Item().withPrimaryKey("raceId", raceInfo.raceId.toHashKeyValue())
                .withInt(RaceInfo::totalNumberOfDnfs.name, raceInfo.totalNumberOfDnfs)
                .withInt(RaceInfo::totalNumberOfDnss.name, raceInfo.totalNumberOfDnss)
                .withInt(RaceInfo::totalNumberOfRacers.name, raceInfo.totalNumberOfRacers)
                .withInt(RaceInfo::year.name, raceInfo.year)
                .withString(RaceInfo::city.name, raceInfo.city)
                .withString(RaceInfo::date.name, SimpleDateFormat("yyyy-MM-dd").format(raceInfo.date))
        val table = dynamoDB.getTable(raceInfoTableName)
        table.putItem(item)
    }

    private fun RaceId.toHashKeyValue(): String {
        return "$index:$year"
    }
}