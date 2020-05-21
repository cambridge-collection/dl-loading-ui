package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;

@Value.Immutable
@Value.Style(builderVisibility = Value.Style.BuilderVisibility.PACKAGE)
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

    public static ImmutableModelStateEnforcementResult successful(
        ModelState<?> state, ResolvedModelStateHandler<?, ?> resolution, Object handlerResult) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.SUCCESSFUL).state(state).resolution(resolution).handlerResult(handlerResult).build();
    }

    public static ImmutableModelStateEnforcementResult resolutionFailed(
        ModelState<?> state, ModelOps.ModelOpsException error) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.RESOLUTION_FAILED).state(state).error(error).build();
    }

    public static ImmutableModelStateEnforcementResult handlerFailed(
        ModelState<?> state, ResolvedModelStateHandler<?, ?> resolution, ModelOps.ModelOpsException error) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.HANDLER_FAILED).state(state).resolution(resolution).error(error).build();
    }
}
