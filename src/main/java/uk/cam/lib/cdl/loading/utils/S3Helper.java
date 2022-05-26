package uk.cam.lib.cdl.loading.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;

public class S3Helper {

    private final AmazonS3 s3;
    private final TransferManager transferManager;

    private static final Logger logger = LoggerFactory.getLogger(S3Helper.class);

    public S3Helper(String region) {
        s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
        transferManager = TransferManagerBuilder.standard().withS3Client(s3).build();
    }


    @PreAuthorize("@roleService.canDeploySites(authentication)")
    public Process syncBucketData(String sourceBucket, String destBucket, boolean deleteIfNotAtSource) throws IOException {

        // Check buckets exist
        if (!s3.doesBucketExistV2(sourceBucket) || !s3.doesBucketExistV2(destBucket)) {
            logger.error("Failed to sync because source or dest bucket does not exist: "+sourceBucket+ " "+destBucket);
            return null;
        }

        logger.info("Starting sync process between: "+sourceBucket+" and "+destBucket);

        String params = " ";
        if (deleteIfNotAtSource) {
            params += " --delete ";
        }

        final String command = "aws s3 sync "+params+" s3://"+sourceBucket+" s3://"+destBucket;
        logger.info("running command: "+command);

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
        return builder.inheritIO().start();

    }

}
