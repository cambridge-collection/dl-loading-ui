package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelStateEnforcementResult.Outcome.*;

@ExtendWith(MockitoExtension.class)
public class ModelOps_EnforceModelStateTest {
    private ModelStateHandler<Integer, Integer> intHandler =
        DefaultModelStateHandler.of(Integer.class, PRESENT, state -> {
            if(state.model() > 10) {
                throw new IOException("I don't like " + state.model());
            }
            return state.model() * 2;
        });
    private ModelStateHandler<String, String> stringHandler =
        DefaultModelStateHandler.of(String.class, state -> {
            if("boom".equals(state.model())) {
                throw new RuntimeException("I went wong");
            }
            return String.format("%s: %s", state.ensure(), state.model());
        });

    private ModelStateHandlerResolver resolver = DefaultModelStateHandlerResolver.builder()
        .addHandlers(intHandler, stringHandler).build();

    @Test
    public void resultsContainStatesInOrder() {
        List<ModelState<?>> states = ImmutableList.of(
            ImmutableModelState.ensure(PRESENT, 1),
            ImmutableModelState.ensure(PRESENT, 2),
            ImmutableModelState.ensure(PRESENT, "foo"),
            ImmutableModelState.ensure(ABSENT, "bar")
        );

        List<?> handlerResults = ImmutableList.of(2, 4, "PRESENT: foo", "ABSENT: bar");

        var results = ModelOps().enforceModelState(states, resolver);

        assertThat(results.stream().map(r -> r.outcome()).allMatch(SUCCESSFUL::equals)).isTrue();
        assertThat(results.stream().map(r -> r.state()).collect(toList()))
            .isEqualTo(states);
        assertThat(results.stream().map(r -> r.handlerResult().orElseThrow()).collect(toList()))
            .isEqualTo(handlerResults);
    }

    @Test
    public void exceptionIsThrownWhenAllStatesAreNotEnforcedSuccessfully() {
        List<ModelState<?>> states = ImmutableList.of(
            ImmutableModelState.ensure(PRESENT, "foo"),
            ImmutableModelState.ensure(ABSENT, "bar"),
            ImmutableModelState.ensure(ABSENT, 1),   // no handler for ABSENT int
            ImmutableModelState.ensure(PRESENT, 100) // handler throws for > 10
        );

        List<ModelStateEnforcementResult.Outcome> outcomes = ImmutableList.of(
            SUCCESSFUL, SUCCESSFUL, RESOLUTION_FAILED, HANDLER_FAILED);

        var exc = Assertions.assertThrows(ModelStateEnforcementFailureException.class, () ->
            ModelOps().enforceModelState(states, resolver));

        // The first state in the stream that failed is used for the message
        assertThat(exc).hasMessageThat().startsWith(
            "Failed to enforce all model states: 2/4 states failed, initial failure: " +
                "No handler found for state: " +
                "ModelState{ensure=ABSENT, type=class java.lang.Integer, model=1}, " +
                "using resolver: ");

        // Exception holds full results
        assertThat(exc.results().stream().map(r -> r.state()).collect(toList()))
            .isEqualTo(states);
        assertThat(exc.results().stream().map(r -> r.outcome()).collect(toList()))
            .isEqualTo(outcomes);

        // The exception's cause is also the first failure
        assertThat(exc.getCause()).isInstanceOf(ModelOpsException.class);
        assertThat(exc.getCause()).isSameInstanceAs(exc.results().get(2).error().orElseThrow());
    }
}
