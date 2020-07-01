package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

public class ImmutableModelStateTest {
    @Test
    public void ensure_factories() {
        var ms = ImmutableModelState.<Integer>builder().ensure(PRESENT).type(Integer.class).model(42).build();
        Truth.assertThat(ImmutableModelState.ensure(PRESENT, Integer.class, 42)).isEqualTo(ms);
        Truth.assertThat(ImmutableModelState.ensure(PRESENT, 42)).isEqualTo(ms);
        Truth.assertThat(ImmutableModelState.ensurePresent(42)).isEqualTo(ms);
        Truth.assertThat(ImmutableModelState.ensureAbsent(42)).isEqualTo(ms.withEnsure(ABSENT));
    }

    @Test
    public void match() {
        var intState = ImmutableModelState.ensure(PRESENT, 42);

        Truth.assertThat(intState.match(Integer.class).isPresent()).isTrue();
        Truth.assertThat(intState.match(Integer.class).orElseThrow()).isSameInstanceAs(intState);

        var numberState = intState.match(Number.class);
        Truth.assertThat(numberState.isPresent()).isTrue();
        Truth.assertThat(numberState.orElseThrow()).isSameInstanceAs(intState);
        Number n = numberState.orElseThrow().model();
        Truth.assertThat(n).isEqualTo(42);

        Truth.assertThat(intState.match(String.class).isEmpty()).isTrue();
    }
}
