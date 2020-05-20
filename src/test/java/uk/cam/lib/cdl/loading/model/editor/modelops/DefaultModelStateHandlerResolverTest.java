package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.truth.Truth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

public class DefaultModelStateHandlerResolverTest {
    @ParameterizedTest
    @MethodSource("resolveHandlerExamples")
    public void resolveHandler(
        DefaultModelStateHandlerResolver resolver,
        ModelState<?> state,
        Optional<ModelStateHandler<?>> expected
    ) {
        resolver.resolveHandler(state).ifPresentOrElse(
            resolution -> {
                Truth.assertThat(expected.isPresent()).isTrue();
                var expectedHandler = expected.orElseThrow();
                Truth.assertThat(resolution.handler()).isSameInstanceAs(expectedHandler);
                Truth.assertThat(resolution.state()).isSameInstanceAs(state);
            },
            () -> {
                Truth.assertThat(expected.isPresent()).isFalse();
            }
        );
    }

    public static Stream<Arguments> resolveHandlerExamples() {
        var presentNumber = DefaultModelStateHandler.of(Number.class, PRESENT, s -> {});
        var anyNumber = DefaultModelStateHandler.of(Number.class, s -> {});
        var anyString = DefaultModelStateHandler.of(String.class, s -> {});

        var resolver = DefaultModelStateHandlerResolver.builder()
            .addHandlers(presentNumber, anyString, anyNumber)
            .build();
        return Stream.of(
            Arguments.of(resolver, ImmutableModelState.ensure(PRESENT, ""), Optional.of(anyString)),
            Arguments.of(resolver, ImmutableModelState.ensure(ABSENT, ""), Optional.of(anyString)),
            Arguments.of(resolver, ImmutableModelState.ensure(PRESENT, 1), Optional.of(presentNumber)),
            Arguments.of(resolver, ImmutableModelState.ensure(ABSENT, 1), Optional.of(anyNumber)),
            // Nothing handles Paths
            Arguments.of(resolver, ImmutableModelState.ensure(ABSENT, Path.of("foo")), Optional.empty())
        );
    }
}
