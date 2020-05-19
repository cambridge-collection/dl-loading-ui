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

    @Test
    public void toStringContainsCollectionIdIfSet() {
        var colWithId = Models.exampleCollection(Path.of("collections/foo.json"));
        Truth.assertThat(colWithId.toString()).contains("@id: collections/foo.json");

        var colWithoutId = Models.exampleCollection(Path.of("collections/foo.json"));
        colWithoutId.setCollectionId(null);
        Truth.assertThat(colWithoutId.toString()).doesNotContain("@id: collections/foo.json");
    }

    @Test
    public void equalsAndHashIgnoreId() {
        var colWithId = Models.exampleCollection(Path.of("collections/foo.json"));
        var colWithoutId = Models.exampleCollection(Path.of("collections/foo.json"));
        colWithoutId.setCollectionId(null);

        Truth.assertThat(colWithId.toString()).isNotEqualTo(colWithoutId.toString());
        Truth.assertThat(colWithId).isEqualTo(colWithoutId);
        Truth.assertThat(colWithId.hashCode()).isEqualTo(colWithoutId.hashCode());
    }
}
