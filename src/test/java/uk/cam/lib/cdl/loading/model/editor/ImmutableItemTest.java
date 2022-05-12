package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Optional;

public class ImmutableItemTest {
    private static final Path ID = Path.of("some/id");

    @ParameterizedTest
    @ValueSource(strings = {
        "/abs/path",
        "non/../normalised/path",
        ""
    })
    public void idMustBeValidId(Path id) {
        Assertions.assertThrows(IllegalStateException.class, () -> ImmutableItem.of(id));
    }

    @Test
    public void of_withId() {
        var item = ImmutableItem.of(ID, "data");
        Truth.assertThat((Object)item.id()).isEqualTo(ID);
    }

    @Test
    public void of_withIdAndFileData() {
        var item = ImmutableItem.of(ID, "data");
        Truth.assertThat((Object)item.id()).isEqualTo(ID);
        Truth.assertThat(item.fileData()).isEqualTo(Optional.of("data"));
    }

    @Test
    public void copyOf() {
        var item = ImmutableItem.of(Path.of("some/id"));
        var reusedItem = ImmutableItem.copyOf(item);
        var copiedItem = ImmutableItem.copyOf(new Item() {
            public Path id() { return Path.of("some/id"); }
            public Optional<String> fileData() { return Optional.empty(); }
        });

        Truth.assertThat(reusedItem).isSameInstanceAs(item);
        Truth.assertThat(copiedItem).isEqualTo(item);
    }
}
