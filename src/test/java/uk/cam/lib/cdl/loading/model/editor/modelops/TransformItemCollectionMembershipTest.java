package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.lang.Nullable;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.ImmutableMembershipDelta;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.testutils.Models;
import uk.cam.lib.cdl.loading.utils.sets.SetMembership;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

public class TransformItemCollectionMembershipTest {
    /**
     * Test transformItemCollectionMembership() by verifying that it just chains
     * {@link ModelOps#calculateCollectionMembershipTransformationDelta} and
     * {@link ModelOps#targetStateForItemCollectionMembershipChange}.
     */
    @Test
    public void transformItemCollectionMembership() {
        var item = ImmutableItem.of(Path.of("example"));
        var collections = ImmutableList.<Collection>of();
        var membershipTx = SetMembership.onlyMemberOf(ImmutableSet.<Path>of());
        @SuppressWarnings("unchecked") var mockDelta =
            (ModelOps.MembershipDelta<Collection>)mock(ModelOps.MembershipDelta.class);
        var result = ImmutableList.<ModelState<?>>of();
        var mockModelOps = mock(ModelOps.class);


        when(mockModelOps.transformItemCollectionMembership(any(), any(), any())).thenCallRealMethod();
        when(mockModelOps.calculateCollectionMembershipTransformationDelta(any(), any(), any())).thenReturn(mockDelta);
        when(mockModelOps.targetStateForItemCollectionMembershipChange(any(), any())).thenReturn(result);

        var actual = mockModelOps.transformItemCollectionMembership(item, collections, membershipTx);

        Truth.assertThat(actual).isSameInstanceAs(result);

        verify(mockModelOps, times(1))
            .transformItemCollectionMembership(item, collections, membershipTx);
        verify(mockModelOps, times(1))
            .calculateCollectionMembershipTransformationDelta(item, collections, membershipTx);
        verify(mockModelOps, times(1))
            .targetStateForItemCollectionMembershipChange(item, mockDelta);
        verifyNoMoreInteractions(mockModelOps);
    }

    private static final Item ITEM = ImmutableItem.of(Path.of("i1"));

    @ParameterizedTest
    @MethodSource("targetStateForItemCollectionMembershipChangeExamples")
    public void targetStateForItemCollectionMembershipChange(
        ModelOps.MembershipDelta<Collection> colDelta,
        @Nullable List<ModelState<?>> targetState,
        @Nullable RuntimeException err
    ) {
        if(targetState != null) {
            Truth.assertThat(ModelOps.ModelOps().targetStateForItemCollectionMembershipChange(ITEM, colDelta))
                .isEqualTo(targetState);
        }
        else {
            Preconditions.checkNotNull(err);
            Truth.assertThat(Assertions.assertThrows(err.getClass(), () ->
                ModelOps.ModelOps().targetStateForItemCollectionMembershipChange(ITEM, colDelta)))
                .hasMessageThat().isEqualTo(err.getMessage());
        }
    }

    public static Stream<Arguments> targetStateForItemCollectionMembershipChangeExamples() {
        // The Item ITEM ("i1") is used for all scenarios
        return Stream.of(
            ImmutableMap.of(
                // 2 collections are defined, with c1 containing i1 and c2 not containing it
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3"),
                    "c2", ImmutableList.of(/* */ "i2", "i3")
                ),
                "target-cols", ImmutableMap.of(
                    "c2", ImmutableList.of("i2", "i3", "i1")
                ),
                // The changes being requested
                "delta", ImmutableMembershipDelta.<String>builder()
                    // The item is added to c2
                    .additions(ImmutableSet.of("c2"))
                    // So i1 is in c1 and c2
                    .membership(ImmutableSet.of("c1", "c2"))
                    .build(),
                "target", ImmutableList.of(
                    // c1 has not changed, c2 has though, so it needs to be updated
                    ImmutableModelState.ensure(PRESENT, "c2")
                )
            ),
            // Empty delta
            ImmutableMap.of(
                // No changes, no membership
                "delta", ImmutableMembershipDelta.<String>builder().build(),
                "target", ImmutableList.of(
                    // Item has no collections, so needs removing
                    ImmutableModelState.ensure(ABSENT, ITEM)
                )
            ),
            // Empty delta with item in a collection
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .membership(ImmutableSet.of("c1"))
                    .build(),
                "target", ImmutableList.of()
            ),
            // Item being removed from all collections
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3"),
                    "c2", ImmutableList.of("i1", "i2", "i3")
                ),
                "target-cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3"),
                    "c2", ImmutableList.of("i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .removals(ImmutableSet.of("c1", "c2"))
                    .build(),
                "target", ImmutableList.of(
                    ImmutableModelState.ensure(ABSENT, ITEM),
                    ImmutableModelState.ensure(PRESENT, "c1"),
                    ImmutableModelState.ensure(PRESENT, "c2")
                )
            ),
            // Adding an item to a collection it's already in (no state change should result)
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .additions(ImmutableSet.of("c1"))
                    .membership(ImmutableSet.of("c1"))
                    .build(),
                "target", ImmutableList.of()
            ),
            // Removing an item from a collection it's not in (no state change should result)
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3"),
                    "c2", ImmutableList.of(/* */ "i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .membership(ImmutableSet.of("c1"))
                    .removals(ImmutableSet.of("c2"))
                    .build(),
                "target", ImmutableList.of()
            ),
            // Item excluded from a single collection
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3")
                ),
                "target-cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .removals(ImmutableSet.of("c1"))
                    .build(),
                "target", ImmutableList.of(
                    ImmutableModelState.ensure(ABSENT, ITEM),
                    ImmutableModelState.ensure(PRESENT, "c1")
                )
            ),
            // Item removed from all previous collections into new one
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i1", "i2", "i3"),
                    "c2", ImmutableList.of("i1", "i2", "i3"),
                    "c3", ImmutableList.of(/* */ "i2", "i3")
                ),
                "target-cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3"),
                    "c2", ImmutableList.of("i2", "i3"),
                    "c3", ImmutableList.of("i2", "i3", "i1")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .membership(ImmutableSet.of("c3"))
                    .additions(ImmutableSet.of("c3"))
                    .removals(ImmutableSet.of("c1", "c2"))
                    .build(),
                "target", ImmutableList.of(
                    ImmutableModelState.ensure(PRESENT, "c3"),
                    ImmutableModelState.ensure(PRESENT, "c1"),
                    ImmutableModelState.ensure(PRESENT, "c2")
                )
            ),
            // Item without any collections being added to one
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3")
                ),
                "target-cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3", "i1")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .membership(ImmutableSet.of("c1"))
                    .additions(ImmutableSet.of("c1"))
                    .build(),
                "target", ImmutableList.of(
                    ImmutableModelState.ensure(PRESENT, "c1")
                )
            ),
            // Adding and removing an item to the same collection - invalid
            ImmutableMap.of(
                "cols", ImmutableMap.of(
                    "c1", ImmutableList.of("i2", "i3")
                ),
                "delta", ImmutableMembershipDelta.<String>builder()
                    .membership(ImmutableSet.of("c1"))
                    .additions(ImmutableSet.of("c1"))
                    .removals(ImmutableSet.of("c1"))
                    .build(),
                "err", new IllegalArgumentException("The same collection occurs in additions and removals")
            )
        ).map(args -> {
            // Build real collections from symbolic representation
            @SuppressWarnings("unchecked") var symbolicCols = (Map<String, List<String>>)args.get("cols");
            @SuppressWarnings("unchecked") var symbolicTargetCols = (Map<String, List<String>>)args.get("target-cols");
            @SuppressWarnings("unchecked") var symbolicDelta = (ModelOps.MembershipDelta<String>)args.get("delta");
            @SuppressWarnings("unchecked") var symbolicTarget = (List<ModelState<String>>)args.get("target");
            var cols = reifySymbolicCollections(symbolicCols == null ? ImmutableMap.of() : symbolicCols);
            var targetCols = reifySymbolicCollections(symbolicTargetCols == null ? ImmutableMap.of() : symbolicTargetCols);
            var err = (RuntimeException)args.get("err");

            return Arguments.of(
                ImmutableMembershipDelta.<Collection>builder()
                    .additions(symbolicDelta.additions().stream().map(cols::get).collect(toImmutableSet()))
                    .removals(symbolicDelta.removals().stream().map(cols::get).collect(toImmutableSet()))
                    .membership(symbolicDelta.membership().stream().map(cols::get).collect(toImmutableSet()))
                    .build(),
                symbolicTarget == null ? null : symbolicTarget.stream()
                    .map(s -> s.type().equals(String.class) ? ImmutableModelState.ensure(s.ensure(), targetCols.get(s.model())) : s)
                    .collect(toImmutableList()),
                err
            );
        });
    }

    private static Map<String, Collection> reifySymbolicCollections(Map<String, List<String>> symbolicCollections) {
        return symbolicCollections.keySet().stream().collect(toImmutableMap(k -> k,
            k -> Models.exampleCollection(Path.of(k), k,
                symbolicCollections.get(k).stream().map(Models.idToReferenceFrom(k)))));
    }
}
