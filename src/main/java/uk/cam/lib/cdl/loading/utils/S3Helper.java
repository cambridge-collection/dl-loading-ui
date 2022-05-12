package uk.cam.lib.cdl.loading.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public class S3Helper {

    private final AmazonS3 s3;
    private final TransferManager transferManager;

    private static final Logger logger = LoggerFactory.getLogger(S3Helper.class);

    public S3Helper(String region) {
        s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
        transferManager = TransferManagerBuilder.standard().withS3Client(s3).build();
    }

    /**
     * TODO Use s3 inventory for faster listing / querying of files
     * TODO move to a separate api
     *
     * @param sourceBucket
     * @param destBucket
     * @param deleteIfNotAtSource
     * @return
     */

    @PreAuthorize("@roleService.canDeploySites(authentication)")
    public synchronized boolean syncBucketData(String sourceBucket, String destBucket, boolean deleteIfNotAtSource) {

        // Check buckets exist
        if (!s3.doesBucketExistV2(sourceBucket) || !s3.doesBucketExistV2(destBucket)) {
            logger.error("Failed to sync because source or dest bucket does not exist: "+sourceBucket+ " "+destBucket);
            return false;
        }

        logger.info("Starting sync process between: "+sourceBucket+" and "+destBucket);

        // Get a list of all objects at source bucket
        ObjectListing sourceListing = s3.listObjects(sourceBucket);
        List<S3ObjectSummary> sourceSummaries = sourceListing.getObjectSummaries();
        while (sourceListing.isTruncated()) {
            sourceListing = s3.listNextBatchOfObjects(sourceListing);
            sourceSummaries.addAll (sourceListing.getObjectSummaries());
        }

        logger.info("Got list of object from source");
        logger.info("Copying objects...");

        // Copy over modified/new objects
        for (S3ObjectSummary sourceObjectSummary: sourceSummaries) {

            String key = sourceObjectSummary.getKey(); // Note key should be the same for source and dest buckets
            logger.info("Key: "+sourceObjectSummary.getKey());

            // If exists already check if modified
            if (!s3.doesObjectExist(destBucket, key)) {
                // copy object if doesn't exist at dest
                //s3.copyObject(sourceBucket, key, destBucket, key);
                logger.info("Does not exist, copying");
                transferManager.copy(sourceBucket, key, destBucket, key);
                continue;
            }


            logger.info("Exist already");
            ObjectMetadata destObjectMetadata = s3.getObjectMetadata(destBucket, key);

            // Overwrite object if modified at source later than at dest
            if (sourceObjectSummary.getLastModified().after(destObjectMetadata.getLastModified())) {
                //s3.copyObject(sourceBucket, key, destBucket, key);
                transferManager.copy(sourceBucket, key, destBucket, key);
                logger.info("is modified, copying");
            }
            logger.info("done");
        }

        if (!deleteIfNotAtSource) {
            return true;
        }

        logger.info("Getting list of objects at destination bucket");
        // Get a list of all objects at dest bucket
        ObjectListing destListing = s3.listObjects(destBucket);
        List<S3ObjectSummary> destSummaries = destListing.getObjectSummaries();
        while (destListing.isTruncated()) {
            destListing = s3.listNextBatchOfObjects(destListing);
            destSummaries.addAll (destListing.getObjectSummaries());
        }

        // Delete any that don't exist at source s3 but do at destination
        for (S3ObjectSummary destObjectSummary: destSummaries) {

            String key = destObjectSummary.getKey(); // Note key should be the same for source and dest buckets

            if (!s3.doesObjectExist(sourceBucket, key)) {
                // delete object if doesn't exist at source
                s3.deleteObject(destBucket, key);
            }
        }

        return true;
    }

}
