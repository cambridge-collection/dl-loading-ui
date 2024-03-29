package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelStateEnforcementResult.Outcome.*;

@ExtendWith(MockitoExtension.class)
public class ImmutableModelStateEnforcementResultTest {
    @Mock private ModelState<?> state;
    @Mock private ResolvedModelStateHandler<?, ?> resolution;
    @Mock private Object handlerResult;
    @Mock private ModelOpsException error;

    @Test
    public void successfulFactory() {
        var a = ImmutableModelStateEnforcementResult.builder().outcome(SUCCESSFUL).state(state)
            .resolution(resolution).handlerResult(handlerResult).build();
        var b = ImmutableModelStateEnforcementResult.successful(state, resolution, handlerResult);
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void successfulFactory_permitsNullHandlerResult() {
        var successfulWithNoResult = ImmutableModelStateEnforcementResult.successful(state, resolution, null);
        assertThat(successfulWithNoResult.handlerResult()).isEqualTo(Optional.empty());
    }

    @Test
    public void resolutionFailedFactory() {
        var a = ImmutableModelStateEnforcementResult.builder().outcome(RESOLUTION_FAILED).state(state)
            .error(error).build();
        var b = ImmutableModelStateEnforcementResult.resolutionFailed(state, error);
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void handlerFailedFactory() {
        var a = ImmutableModelStateEnforcementResult.builder().outcome(HANDLER_FAILED).state(state)
            .resolution(resolution).error(error).build();
        var b = ImmutableModelStateEnforcementResult.handlerFailed(state, resolution, error);
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testPropertyCombinationValidation() {
        // Missing required properties
        assertThrows(IllegalStateException.class, () -> ImmutableModelStateEnforcementResult.builder().build());
        assertThrows(IllegalStateException.class, () -> ImmutableModelStateEnforcementResult.builder().outcome(SUCCESSFUL).build());
        assertThrows(IllegalStateException.class, () -> ImmutableModelStateEnforcementResult.builder().state(state).build());

        var successful = ImmutableModelStateEnforcementResult.builder().outcome(SUCCESSFUL).state(state)
            .resolution(resolution).handlerResult(handlerResult).build();
        assertThrows(IllegalStateException.class, () -> successful.withResolution(Optional.empty()));
        assertThat(successful.withHandlerResult(Optional.empty()).handlerResult()).isEqualTo(Optional.empty());
        assertThrows(IllegalStateException.class, () -> successful.withError(error));

        var resolutionFailed = ImmutableModelStateEnforcementResult.builder().outcome(RESOLUTION_FAILED).state(state)
            .error(error).build();
        assertThrows(IllegalStateException.class, () -> resolutionFailed.withResolution(resolution));
        assertThrows(IllegalStateException.class, () -> resolutionFailed.withHandlerResult(handlerResult));
        assertThrows(IllegalStateException.class, () -> resolutionFailed.withError(Optional.empty()));

        var handlerFailed = ImmutableModelStateEnforcementResult.builder().outcome(HANDLER_FAILED).state(state)
            .resolution(resolution).error(error).build();
        assertThrows(IllegalStateException.class, () -> handlerFailed.withResolution(Optional.empty()));
        assertThrows(IllegalStateException.class, () -> handlerFailed.withHandlerResult(handlerResult));
        assertThrows(IllegalStateException.class, () -> handlerFailed.withError(Optional.empty()));

        // messages
        assertThat(assertThrows(IllegalStateException.class, () -> resolutionFailed.withHandlerResult(Optional.of(1))))
            .hasMessageThat().isEqualTo("handlerResult must not be present with outcome RESOLUTION_FAILED");
        assertThat(assertThrows(IllegalStateException.class, () -> successful.withError(error)))
            .hasMessageThat().isEqualTo("error must not be present with outcome SUCCESSFUL");
    }
}
