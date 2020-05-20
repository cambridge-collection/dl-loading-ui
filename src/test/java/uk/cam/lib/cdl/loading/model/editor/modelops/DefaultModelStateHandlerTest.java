package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.cam.lib.cdl.loading.model.editor.modelops._DefaultModelStateHandler.ThrowingConsumer;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

@ExtendWith(MockitoExtension.class)
@TestInstance(PER_CLASS)
public class DefaultModelStateHandlerTest {
    private static final DefaultModelStateHandler<Object> NOOP_HANDLER = DefaultModelStateHandler.of(Object.class, (s) -> {});

    @Mock
    private ThrowingConsumer<ModelState<? extends Number>, IOException> handlerFunc;

    DefaultModelStateHandler<Number> numberHandler;
    DefaultModelStateHandler<Number> numberEnsurePresent;
    DefaultModelStateHandler<Number> numberEnsureAbsent;

    @BeforeAll
    private void beforeAll() {
        numberHandler = DefaultModelStateHandler.of(Number.class, s -> this.handlerFunc.accept(s));
        numberEnsurePresent = numberHandler.withSupportedEnsureValues(PRESENT);
        numberEnsureAbsent = numberHandler.withSupportedEnsureValues(ABSENT);
    }

    @Test
    public void forState_rejectsNullValues() {
        Assertions.assertThrows(NullPointerException.class, () -> NOOP_HANDLER.match(null));
    }

    @ParameterizedTest
    @MethodSource("match_matchesExpectedStatesExamples")
    public <T> void match_matchesExpectedStates(DefaultModelStateHandler<?> handler, ModelState<T> state, boolean matches) {
        var match = handler.match(state);

        Truth.assertThat(match).isNotNull();
        match.ifPresentOrElse((ModelStateHandler<? super T> typedHandler) -> {
            Truth.assertThat(matches).isTrue();
            Truth.assertThat(typedHandler).isSameInstanceAs(handler);
        }, () -> {
            Truth.assertThat(matches).isFalse();
        });
    }

    public Stream<Arguments> match_matchesExpectedStatesExamples() {
        return Stream.of(
            Arguments.of(numberHandler, ImmutableModelState.ensure(PRESENT, Number.class, 1), true),
            Arguments.of(numberHandler, ImmutableModelState.ensure(ABSENT, Number.class, 1), true),

            Arguments.of(numberHandler, ImmutableModelState.ensure(PRESENT, 1), true),
            Arguments.of(numberHandler, ImmutableModelState.ensure(ABSENT, 1), true),
            Arguments.of(numberHandler, ImmutableModelState.ensure(PRESENT, 1f), true),
            Arguments.of(numberHandler, ImmutableModelState.ensure(ABSENT, 1f), true),
            Arguments.of(numberHandler, ImmutableModelState.ensure(PRESENT, "1"), false),
            Arguments.of(numberHandler, ImmutableModelState.ensure(ABSENT, "1"), false),

            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(PRESENT, 1), true),
            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(ABSENT, 1), false),
            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(PRESENT, 1f), true),
            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(ABSENT, 1f), false),
            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(PRESENT, "1"), false),
            Arguments.of(numberEnsurePresent, ImmutableModelState.ensure(ABSENT, "1"), false),

            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(PRESENT, 1), false),
            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(ABSENT, 1), true),
            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(PRESENT, 1f), false),
            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(ABSENT, 1f), true),
            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(PRESENT, "1"), false),
            Arguments.of(numberEnsureAbsent, ImmutableModelState.ensure(ABSENT, "1"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("handleCanBeCalledWithoutCastingAfterMatchingExamples")
    public <T extends Number> void handlerMethodInvocation(
        DefaultModelStateHandler<?> handler, ModelState<T> state
    ) throws IOException {
        var match = handler.match(state);

        ModelStateHandler<? super T> typedHandler = match.orElseThrow();
        Assertions.assertDoesNotThrow(() -> typedHandler.handle(state));
        Mockito.verify(handlerFunc, Mockito.times(1)).accept(state);
        Mockito.verifyNoMoreInteractions(handlerFunc);
    }

    public Stream<Arguments> handleCanBeCalledWithoutCastingAfterMatchingExamples() {
        // Only the matching examples
        return match_matchesExpectedStatesExamples()
            .filter(arg -> (boolean)arg.get()[2])
            .map(arg -> Arguments.of(arg.get()[0], arg.get()[1]));
    }
}
