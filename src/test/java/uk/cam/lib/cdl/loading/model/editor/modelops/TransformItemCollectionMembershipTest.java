package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.ImmutableMembershipDelta;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.utils.sets.SetMembershipTransformation;

import java.nio.file.Path;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransformItemCollectionMembershipTest {
    @Test
    public void transformItemCollectionMembership() {
        var item = Mockito.mock(Item.class, "the-item");
        var collections = IntStream.range(0, 10).mapToObj(i -> Mockito.mock(Collection.class, "col-" + i))
            .collect(toImmutableList());
        var added = collections.subList(0, 3);
        var removed = collections.subList(5, 7);
        var membership = collections.subList(0, 4);
        @SuppressWarnings("unchecked")
        var transform = (SetMembershipTransformation<Path>)Mockito.mock(SetMembershipTransformation.class);
        var delta = ImmutableMembershipDelta.<Collection>builder()
            .additions(ImmutableSet.copyOf(added))
            .removals(ImmutableSet.copyOf(removed))
            .membership(ImmutableSet.copyOf(membership))
            .build();

        // We test by mocking all the MockOps methods except the one under test
        var mockModelOps = Mockito.mock(ModelOps.class);
        when(mockModelOps.transformItemCollectionMembership(any(), any(), any()))
            .thenCallRealMethod();
        // Have the delta calculation method return our mock data
        when(mockModelOps.calculateCollectionMembershipTransformationDelta(any(), any(), any()))
            .thenReturn(delta);

        // Call the real method
        var resultDelta = mockModelOps.transformItemCollectionMembership(item, collections, transform);

        // Ensure the union of added and removed collections is returned
        Truth.assertThat(resultDelta).isSameInstanceAs(delta);

        // Ensure it used the calculation method to obtain the result
        verify(mockModelOps, times(1))
            .calculateCollectionMembershipTransformationDelta(item, collections, transform);
        // Ensure it added/removed the expected collections
        added.forEach(col -> verify(mockModelOps, times(1)).addItemToCollection(col, item));
        removed.forEach(col -> verify(mockModelOps, times(1)).removeItemFromCollection(col, item));
    }
}
