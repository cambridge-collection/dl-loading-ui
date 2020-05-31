package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Value.Immutable
@Value.Style(builderVisibility = Value.Style.BuilderVisibility.PACKAGE)
public abstract class AbstractModelStateEnforcementResult implements ModelStateEnforcementResult {
    @Value.Check
    void validate() {
        checkState(resolution().isPresent(), outcome() == Outcome.SUCCESSFUL || outcome() == Outcome.HANDLER_FAILED,
            "resolution %s be present with outcome %s");
        Preconditions.checkState(outcome() == Outcome.SUCCESSFUL || handlerResult().isEmpty(),
            "handlerResult must not be present with outcome %s", outcome());
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
        ModelState<?> state, ResolvedModelStateHandler<?, ?> resolution, @Nullable Object handlerResult) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.SUCCESSFUL)
            .state(state)
            .resolution(resolution)
            .handlerResult(Optional.ofNullable(handlerResult))
            .build();
    }

    public static ImmutableModelStateEnforcementResult resolutionFailed(
        ModelState<?> state, ModelOpsException error) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.RESOLUTION_FAILED).state(state).error(error).build();
    }

    public static ImmutableModelStateEnforcementResult handlerFailed(
        ModelState<?> state, ResolvedModelStateHandler<?, ?> resolution, ModelOpsException error) {
        return ImmutableModelStateEnforcementResult.builder()
            .outcome(Outcome.HANDLER_FAILED).state(state).resolution(resolution).error(error).build();
    }
}
