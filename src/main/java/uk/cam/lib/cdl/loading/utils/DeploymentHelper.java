package uk.cam.lib.cdl.loading.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;

public class DeploymentHelper {

    private final String sourceBucket;
    private final String sourceTranscriptionBucket;
    private final String destBucket;
    private final String destTranscriptionBucket;
    private final S3Helper s3Helper;
    private final String dataSyncTaskARN;

    private static final Logger logger = LoggerFactory.getLogger(DeploymentHelper.class);

    public DeploymentHelper(String region, String sourceBucket,
                            String sourceTranscriptionBucket,
                            String destBucket,
                            String destTranscriptionBucket,
                            String datasyncTaskARN) {
        this.sourceBucket = sourceBucket;
        this.sourceTranscriptionBucket = sourceTranscriptionBucket;
        this.destBucket = destBucket;
        this.destTranscriptionBucket = destTranscriptionBucket;
        this.s3Helper = new S3Helper(region);
        this.dataSyncTaskARN = datasyncTaskARN;
    }
    public boolean deploy() throws IOException, InterruptedException {
        Process returnOK = s3Helper.syncBucketData(sourceBucket,destBucket, true);
        Process transReturnOK = s3Helper.syncBucketData(sourceTranscriptionBucket, destTranscriptionBucket, true);

        logger.info("Waiting for items to be copied to production S3..");
        // Wait for data s3 transfer to complete before s3 to efs transfer (via datasync)
        returnOK.waitFor();
        logger.info("Syncing production S3 item data to production EFS");
        runDataSyncTask(dataSyncTaskARN);
        logger.info("Deploy complete");

        return true;
    }

    /**
     * Using DataSync to copy data from production s3 volume to production EFS volume
     * NOTE: ENSURE S3 COPY IS COMPLETE BEFORE PROCESSING
     *
     * @param arn
     * @return
     * @throws IOException
     */
    @PreAuthorize("@roleService.canDeploySites(authentication)")
    public Process runDataSyncTask(String arn) throws IOException {
        final String command = "aws datasync start-task-execution --task-arn "+arn;
        logger.info("running command: "+command);

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
        return builder.inheritIO().start();

    }
}
