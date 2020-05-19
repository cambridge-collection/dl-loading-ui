package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import org.immutables.value.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.ImmutableMembershipDelta;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.utils.sets.SetMembership;
import uk.cam.lib.cdl.loading.utils.sets.SetMembershipTransformation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static uk.cam.lib.cdl.loading.testutils.Models.exampleCollection;
import static uk.cam.lib.cdl.loading.testutils.Models.idToReferenceFrom;

public class CalculateCollectionMembershipTransformationDeltaTest {
    @ParameterizedTest
    @MethodSource("calculateCollectionMembershipTransformationDeltaExamples")
    public void calculateCollectionMembershipTransformationDelta(
        Item item,
        Iterable<Collection> collections,
        SetMembershipTransformation<Path> membershipTransformation,
        ModelOps.MembershipDelta<Collection> expectedDelta
    ) {
        Truth.assertThat(ModelOps.ModelOps().calculateCollectionMembershipTransformationDelta(
            item, collections, membershipTransformation)).isEqualTo(expectedDelta);
    }

    static Stream<Arguments> calculateCollectionMembershipTransformationDeltaExamples() {
        return Stream.of(
            exampleBuilder()
                // Add/Remove item with ID 0
                .itemId(0)
                // 3 collections (0, 1 and 2) exist, containing items 1-6
                .putCollection(0, ImmutableList.of(1, 2))
                .putCollection(1, ImmutableList.of(3, 4))
                .putCollection(2, ImmutableList.of(5, 6))
                // We want our item to be a member of only collections 0 and 2
                .membershipTransform(MembershipTx.onlyMemberOf().withPopulation(0, 2))
                // It's currently not in any, so we expect it to be added to 0 and 2
                .addExpectedAdditions(0, 2)
                // And not removed from anything
                .addExpectedRemovals()
                // So the item will be a member of 0 and 2
                .addExpectedMembership(0, 2)
                .build(),
            exampleBuilder()
                .itemId(0)
                .putCollection(0, ImmutableList.of(0, 1, 2))
                .putCollection(1, ImmutableList.of(0, 3, 4))
                .putCollection(2, ImmutableList.of(5, 6))
                .membershipTransform(MembershipTx.onlyMemberOf().withPopulation(0, 2))
                .addExpectedAdditions(2)
                .addExpectedRemovals(1)
                .addExpectedMembership(0, 2)
                .build(),
            exampleBuilder()
                .itemId(0)
                .putCollection(0, ImmutableList.of(0, 1, 2))
                .putCollection(1, ImmutableList.of(0, 3, 4))
                .putCollection(2, ImmutableList.of(5, 6))
                .putCollection(3, ImmutableList.of(7, 8))
                .membershipTransform(MembershipTx.addingAndRemoving()
                    .withExcluded(0)
                    .withIncluded(1, 2, 3))
                .addExpectedAdditions(2, 3)
                .addExpectedRemovals(0)
                .addExpectedMembership(1, 2, 3)
                .build(),
            // Noop
            exampleBuilder()
                .itemId(0)
                .membershipTransform(MembershipTx.addingAndRemoving()
                    .withExcluded()
                    .withIncluded())
                .addExpectedAdditions()
                .addExpectedRemovals()
                .addExpectedMembership()
                .build(),
            // Noop
            exampleBuilder()
                .itemId(0)
                .membershipTransform(MembershipTx.onlyMemberOf()
                    .withPopulation())
                .addExpectedAdditions()
                .addExpectedRemovals()
                .addExpectedMembership()
                .build(),
            exampleBuilder()
                .itemId(0)
                .putCollection(0, ImmutableList.of(0, 1, 2))
                .putCollection(1, ImmutableList.of(3, 4))
                .putCollection(2, ImmutableList.of(0, 5, 6))
                .membershipTransform(MembershipTx.addingAndRemoving()
                    .withExcluded()
                    .withIncluded())
                .addExpectedAdditions()
                .addExpectedRemovals()
                .addExpectedMembership(0, 2)
                .build()
        ).map(CalculateCollectionMembershipTransformationDeltaExample::makeArguments);
    }

    static ImmutableCalculateCollectionMembershipTransformationDeltaExample.Builder exampleBuilder() {
        return ImmutableCalculateCollectionMembershipTransformationDeltaExample.builder();
    }

    @Value.Immutable
    @Value.Style(depluralize = true)
    interface CalculateCollectionMembershipTransformationDeltaExample {
        int itemId();
        Map<Integer, List<Integer>> collections();
        MembershipTx membershipTransform();
        Set<Integer> expectedAdditions();
        Set<Integer> expectedRemovals();
        Set<Integer> expectedMembership();

        default Path makeId(String thing, int n) { return Path.of(String.format("%ss/%s-%d.json", thing, thing, n)); }
        default Path makeItemId(int n) { return makeId("item", n); }
        default Path makeColId(int n) { return makeId("collection", n); }
        default Item makeItem(int n) { return ImmutableItem.of(makeItemId(n)); }
        default Collection makeCollection(int n, List<Integer> items) {
            var colId = makeColId(n);
            return exampleCollection(colId, items.stream().map(this::makeItemId).map(idToReferenceFrom(colId)));
        }

        default Arguments makeArguments() {
            var item = makeItem(itemId());
            var collections = collections().keySet().stream()
                .collect(toImmutableMap(colN -> colN, colN -> makeCollection(colN, collections().get(colN))));
            var membershipTx = membershipTransform().getTx(this::makeColId);
            var delta = ImmutableMembershipDelta.builder()
                .additions(expectedAdditions().stream().map(collections::get).collect(toImmutableSet()))
                .removals(expectedRemovals().stream().map(collections::get).collect(toImmutableSet()))
                .membership(expectedMembership().stream().map(collections::get).collect(toImmutableSet()))
                .build();
            return Arguments.of(item, collections.values(), membershipTx, delta);
        }
    }

    interface MembershipTx {
        <T> SetMembershipTransformation<T> getTx(Function<Integer, ? extends T> mapping);
        static ImmutableOnlyMemberOfTx onlyMemberOf() { return ImmutableOnlyMemberOfTx.of(ImmutableSet.of()); }
        static ImmutableAddingAndRemovingTx addingAndRemoving() { return ImmutableAddingAndRemovingTx.of(ImmutableSet.of(), ImmutableSet.of()); };
    }

    @Value.Immutable
    interface OnlyMemberOfTx extends MembershipTx {
        @Value.Parameter(order = 0)
        Set<Integer> population();

        @Override
        default <T> SetMembershipTransformation<T> getTx(Function<Integer, ? extends T> mapping) {
            return SetMembership.onlyMemberOf(population().stream().map(mapping).collect(toImmutableSet()));
        }
    }

    @Value.Immutable
    interface AddingAndRemovingTx extends MembershipTx {
        @Value.Parameter(order = 0)
        Set<Integer> included();
        @Value.Parameter(order = 0)
        Set<Integer> excluded();

        @Override
        default <T> SetMembershipTransformation<T> getTx(Function<Integer, ? extends T> mapping) {
            return SetMembership.addingAndRemoving(
                included().stream().map(mapping).collect(toImmutableSet()),
                excluded().stream().map(mapping).collect(toImmutableSet()));
        }
    }
}
