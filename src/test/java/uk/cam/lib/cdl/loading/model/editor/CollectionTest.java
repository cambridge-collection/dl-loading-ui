package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.cam.lib.cdl.loading.testutils.Models;

import java.nio.file.Path;
import java.util.stream.Stream;

public class CollectionTest {
    public static Stream<Arguments> copyOfExamples() {
        var col = Models.exampleCollection(Path.of("c1"),
            Stream.of("i1", "i2").map(Models.idToReferenceFrom("c1")));
        var nullCol = new Collection(
            new CollectionName(null, null, null, null),
            null, null, ImmutableList.of());

        return Stream.of(
            Arguments.of(col),
            Arguments.of(nullCol)
        );
    }

    @ParameterizedTest
    @MethodSource("copyOfExamples")
    public void copyOf(Collection col) {
        var copy = Collection.copyOf(col);

        Truth.assertThat(col).isNotSameInstanceAs(copy);
        Truth.assertThat(col).isEqualTo(copy);
        // immutable members
        Truth.assertThat(col.getName()).isSameInstanceAs(copy.getName());
        Truth.assertThat(col.getThumbnailURL()).isSameInstanceAs(copy.getThumbnailURL());
        Truth.assertThat(col.getCollectionId()).isSameInstanceAs(copy.getCollectionId());
        // non immutable members
        assertBothNullOrDistinctInstances(col.getDescription(), copy.getDescription());
        assertBothNullOrDistinctInstances(col.getCredit(), copy.getCredit());
        Truth.assertThat(col.getItemIds()).isNotSameInstanceAs(copy.getItemIds());
    }

    private <T> void assertBothNullOrDistinctInstances(T a, T b) {
        Truth.assertThat(a).isEqualTo(b);
        if(a != null) {
            Truth.assertThat(a).isNotSameInstanceAs(b);
        }
    }

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
