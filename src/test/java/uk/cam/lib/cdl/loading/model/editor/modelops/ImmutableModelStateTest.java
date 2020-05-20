package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

public class ImmutableModelStateTest {
    @Test
    public void match() {
        var intState = ImmutableModelState.ensure(ModelState.Ensure.PRESENT, 42);

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
