package uk.cam.lib.cdl.loading.model.packaging;

import java.util.Date;

public class Pipeline {

    private final String id;
    private final int buildNumber;
    private final Date created;
    private final PipelineStatus status;
    private final Date completed;

    public Pipeline(String id, int buildNumber, Date created, PipelineStatus status, Date completed) {

        this.id = id;
        this.buildNumber = buildNumber;
        this.created = created;
        this.status = status;
        this.completed = completed;
    }

    public Date getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public Date getCompleted() {
        return completed;
    }

    public int getBuildNumber() {
        return buildNumber;
    }
}
