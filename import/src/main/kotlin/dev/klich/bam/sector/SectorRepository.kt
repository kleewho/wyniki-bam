package dev.klich.bam.sector

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.klich.bam.BibNumber
import dev.klich.bam.RaceId
import dev.klich.bam.RaceInfo
import dev.klich.bam.SectorRacerInformation
import dev.klich.bam.repository.InMemoryRaceInfoRepository
import java.time.Duration

typealias Season = Int

interface SectorRacerInformationRepository {
    fun find(season: Season, bibNumber: BibNumber): SectorRacerInformation?

    fun store(sectorRacerInformation: SectorRacerInformation)
}

class InMemorySectorRepository {


    fun find(bibNumber: BibNumber): SectorRacerInformation? {
        TODO()
    }


    fun store(sectorRacerInformation: SectorRacerInformation) {
        TODO()
    }
}

class DynamoDBSectorRacerInformationRepository(private val dynamoDB: DynamoDB) : SectorRacerInformationRepository {
    private val sectorRacerInformationTableName = "sector-racer-information"
    private val inMemoryRaceInfoRepository = InMemoryRaceInfoRepository()
    private val objectMapper = jacksonObjectMapper()

    companion object {
        fun create(): SectorRacerInformationRepository {
            val dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("dynamodb.eu-central-1.amazonaws.com", "eu-central-1"))
                    .build()
            val dynamoDB = DynamoDB(dynamoDBClient)
            return DynamoDBSectorRacerInformationRepository(dynamoDB = dynamoDB)
        }

    }
    override fun find(season: Season, bibNumber: BibNumber): SectorRacerInformation? {
        val table = dynamoDB.getTable(sectorRacerInformationTableName)
        return table.getItem("sectorRacerInfoId", hashKey(season = season, bibNumber = bibNumber))?.let { item ->
            objectMapper.readValue<SectorRacerInformation>(item.getJSON("value"))
        }
    }

    override fun store(sectorRacerInformation: SectorRacerInformation) {
        val item = Item().withPrimaryKey("sectorRacerInfoId", sectorRacerInformation.toHashKeyValue())
                .withJSON("value", objectMapper.writeValueAsString(sectorRacerInformation))

        val table = dynamoDB.getTable(sectorRacerInformationTableName)
        table.putItem(item)
    }

    private fun SectorRacerInformation.toHashKeyValue(): String {
        return hashKey(season = season, bibNumber = bibNumber)
    }

    private fun hashKey(season: Season, bibNumber: BibNumber): String {
        return "$season:$bibNumber"
    }
}