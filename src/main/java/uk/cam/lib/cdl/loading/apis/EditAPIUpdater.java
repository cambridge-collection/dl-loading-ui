package uk.cam.lib.cdl.loading.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class EditAPIUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(EditAPIUpdater.class);

    private final EditAPI editAPI;

    public EditAPIUpdater(@NotNull EditAPI editAPI) {
        this.editAPI = editAPI;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 500) // Every 5 mins
    public void checkForUpdates() throws IOException {
        LOG.info("Updating model...");
        editAPI.updateModel();
    }
}
