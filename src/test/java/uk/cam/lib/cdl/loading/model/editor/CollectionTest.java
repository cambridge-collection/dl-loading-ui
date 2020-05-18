package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.cam.lib.cdl.loading.testutils.Models;

import java.nio.file.Path;

public class CollectionTest {
    @Test
    public void getIdAsPath() {
        var path = Path.of("some/path.json");
        var col = Models.exampleCollection(path);
        Truth.assertThat((Object)col.getIdAsPath()).isEqualTo(path);
    }

    @Test
    public void getIdAsPathThrowsIfIdNotSet() {
        var path = Path.of("some/path.json");
        var col = Models.exampleCollection(path);
        col.setCollectionId(null);
        Truth.assertThat(Assertions.assertThrows(NullPointerException.class, col::getIdAsPath))
            .hasMessageThat().isEqualTo("collection has no ID set");
    }
}
