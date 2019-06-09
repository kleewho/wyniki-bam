package dev.klich.bam

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula.ObjectExtractor
import technology.tabula.Rectangle
import technology.tabula.detectors.NurminenDetectionAlgorithm

class CsvResultsHandler {
    fun handle(s3Event: S3Event, context: Context) {
        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(DefaultAWSCredentialsProviderChain()).build()

        s3Event.records.forEach {
            val s3Key = it.s3.getObject().key
            val s3Bucket = it.s3.bucket.name
            context.logger.log("found id: $s3Bucket $s3Key")
            val s3Obj = s3Client.getObject(GetObjectRequest(s3Bucket, s3Key))
            context.logger.log("content type: ${s3Obj.objectMetadata.contentType}")

            val document = PDDocument.load(s3Obj.objectContent)
            val objectExtractor = ObjectExtractor(document)
            val page = objectExtractor.extract(0)

            val detectionAlgorithm = NurminenDetectionAlgorithm()

            page.setRect(firstPageRectangle)

        }
    }

    val firstPageRectangle = Rectangle(59.44f, 17.89f, 804.86f, 502.90f)
    val otherPagesRectangle = Rectangle(80.48f, 17.89f, 804.86f, 482.91f)

}