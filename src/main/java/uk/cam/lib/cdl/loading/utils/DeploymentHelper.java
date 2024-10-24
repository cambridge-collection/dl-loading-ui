package uk.cam.lib.cdl.loading.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;

public class DeploymentHelper {

    private final String sourceBucket;
    private final String destBucket;
    private final S3Helper s3Helper;

    private static final Logger logger = LoggerFactory.getLogger(DeploymentHelper.class);

    public DeploymentHelper(String region, String sourceBucket,
                            String destBucket) {
        this.sourceBucket = sourceBucket;
        this.destBucket = destBucket;
        this.s3Helper = new S3Helper(region);

    }

    @PreAuthorize("@roleService.canDeploySites(authentication)")
    public boolean deploy() throws IOException, InterruptedException {
        Process returnOK = s3Helper.syncBucketData(sourceBucket,destBucket, true);

        logger.info("Waiting for items to be copied to production S3..");
        // Wait for data s3 transfer to complete before s3 to efs transfer (via datasync)
        returnOK.waitFor();
        logger.info("Deploy complete");

        return true;
    }

}
