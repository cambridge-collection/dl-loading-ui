package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ModelOps_EnforceModelStateTest {
    @Test
    public void enforceModelState_throwsForFirstUnhandledState() {
        Mockito.mock(ModelStateHandlerResolver.class);
        // TODO: modify state enforcement func to return enforcement result
    }
}
