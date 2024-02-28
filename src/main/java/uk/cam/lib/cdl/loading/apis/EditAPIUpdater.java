package uk.cam.lib.cdl.loading.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;

import javax.validation.constraints.NotNull;

public class EditAPIUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(EditAPIUpdater.class);

    private final EditAPI editAPI;

    public EditAPIUpdater(@NotNull EditAPI editAPI) {
        this.editAPI = editAPI;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 60 * 1000) // Every 5 mins
    public void checkForUpdates() {
        LOG.info("Updating model...");
        try {
            editAPI.updateModel();
        }
        catch (EditApiException e) {
            LOG.error("EditAPI failed to update its model: " + e.getMessage(), e);
        }
    }
}
