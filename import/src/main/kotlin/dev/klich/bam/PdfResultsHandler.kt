package dev.klich.bam

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import dev.klich.bam.repository.DynamoDBRaceInfoRepository

class PdfResultsHandler {
    fun handle(s3Event: S3Event, context: Context) {
        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(DefaultAWSCredentialsProviderChain()).build()

        s3Event.records.forEach { record ->
            val s3Key =  record.s3.getObject().key
            val s3Bucket =  record.s3.bucket.name
            context.logger.log("found id: $s3Bucket $s3Key")
            val s3Obj = s3Client.getObject(GetObjectRequest(s3Bucket, s3Key))
            context.logger.log("content type: ${s3Obj.objectMetadata.contentType}")

            try {
                val raceInfoRepository = DynamoDBRaceInfoRepository.create()
                val tables = extractTables(s3Obj.objectContent)!!
                val bamResults = tables.flatMap { it.toBamResults(Races.rybnik) }
                context.logger.log("Found ${bamResults.size} BamResult objects")
                val raceInfo = RaceInfo(raceId = Races.rybnik, bestHobbyResult = bamResults.getFastestResult(BamDistance.HOBBY).totalTime!!,
                        bestProResult = bamResults.getFastestResult(BamDistance.PRO).totalTime!!,
                        numberOfDnfs = bamResults.count { it.dnf })

                context.logger.log("Storing raceInfo $raceInfo")
                raceInfoRepository.store(raceInfo)
                context.logger.log("RaceInfo ${raceInfo.raceId} stored successfully")
            } catch (t: Throwable) {
                context.logger.log("caught throwable $t")
                throw t
            }

        }
    }
}
