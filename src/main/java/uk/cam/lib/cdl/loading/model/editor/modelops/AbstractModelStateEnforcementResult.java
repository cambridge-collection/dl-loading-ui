package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractModelStateEnforcementResult implements ModelStateEnforcementResult {
    @Value.Check
    void validate() {
        checkState(resolution().isPresent(), outcome() == Outcome.SUCCESSFUL || outcome() == Outcome.HANDLER_FAILED,
            "resolution %s be present with outcome %s");
        checkState(handlerResult().isPresent(), outcome() == Outcome.SUCCESSFUL,
            "handlerResult %s be present with outcome %s");
        checkState(error().isPresent(), outcome() == Outcome.RESOLUTION_FAILED || outcome() == Outcome.HANDLER_FAILED,
            "error %s be present with outcome %s");
    }

    private void checkState(boolean condition, boolean expected, String msgTemplate) {
        checkState(condition, expected, msgTemplate, "must", "must not");
    }
    private void checkState(boolean condition, boolean expected, String msgTemplate, String expectedTerm, String notExpectedTerm) {
        Preconditions.checkState(condition == expected,
            msgTemplate, expected ? expectedTerm : notExpectedTerm, outcome());
    }
}
