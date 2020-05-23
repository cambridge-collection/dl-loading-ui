package uk.cam.lib.cdl.loading.utils.sets;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

public class SetMembership {
    private SetMembership() {}

    private static <T> void validateMembersAndUniverse(Set<T> members, Set<T> universe) {
        var alienMembers = Sets.difference(members, universe);
        Preconditions.checkArgument(alienMembers.isEmpty(),
            "members contains elements not present in universe: %s", alienMembers);
    }

    public static <T> SetMembershipTransformation<T> onlyMemberOf(Set<T> population) {
        return (members, universe) -> {
            validateMembersAndUniverse(members, universe);
            return ImmutableSetMembershipTransformationResult.<T>builder()
                .members(Sets.intersection(population, universe))
                .excludedAliens(ImmutableSet.of())
                .includedAliens(Sets.difference(population, universe))
                .build();
        };
    }

    public static <T> SetMembershipTransformation<T> addingAndRemoving(Set<T> included, Set<T> excluded) {
        var argumentOverlap = Sets.intersection(excluded, included);
        Preconditions.checkArgument(argumentOverlap.isEmpty(), "excluded and included conflict - both contain: %s", argumentOverlap);
        return (members, universe) -> {
            validateMembersAndUniverse(members, universe);
            return ImmutableSetMembershipTransformationResult.<T>builder()
                .members(Sets.union(Sets.difference(members, excluded), Sets.intersection(included, universe)))
                .excludedAliens(Sets.difference(excluded, universe))
                .includedAliens(Sets.difference(included, universe))
                .build();
        };
    }

    /**
     * Get a transform which makes no changes.
     */
    public static <T> SetMembershipTransformation<T> unchanged() {
        return addingAndRemoving(ImmutableSet.of(), ImmutableSet.of());
    }
}
