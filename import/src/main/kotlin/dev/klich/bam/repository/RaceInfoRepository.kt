package dev.klich.bam.repository

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import dev.klich.bam.RaceId
import dev.klich.bam.RaceInfo
import java.time.Duration

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
    private val raceInfoTableName = "race-info"
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
                    numberOfDnfs = item.getInt(RaceInfo::numberOfDnfs.name),
                    bestProResult = Duration.parse(item.getString(RaceInfo::bestProResult.name)),
                    bestHobbyResult= Duration.parse(item.getString(RaceInfo::bestHobbyResult.name))
                    ).also { inMemoryRaceInfoRepository.store(it) }
        }

    }

    override fun store(raceInfo: RaceInfo) {
        val item = Item().withPrimaryKey("raceId", raceInfo.raceId.toHashKeyValue())
                .withString(RaceInfo::bestHobbyResult.name, raceInfo.bestHobbyResult.toString())
                .withString(RaceInfo::bestProResult.name, raceInfo.bestProResult.toString())
                .withString(RaceInfo::numberOfDnfs.name, raceInfo.numberOfDnfs.toString())
        val table = dynamoDB.getTable(raceInfoTableName)
        table.putItem(item)
    }

    private fun RaceId.toHashKeyValue(): String {
        return "$index:$year"
    }
}