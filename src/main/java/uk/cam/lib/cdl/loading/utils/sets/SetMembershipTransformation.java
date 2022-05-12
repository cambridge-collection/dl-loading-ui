package uk.cam.lib.cdl.loading.utils.sets;

import com.google.common.collect.ImmutableSet;
import org.immutables.value.Value;

import java.util.Set;

public interface SetMembershipTransformation<T> {
    Result<T> appliedTo(Set<T> members, Set<T> universe);

    @Value.Immutable
    @Value.Style(
        // Disable copying of set properties into immutable set types — we use
        // set views and don't want to copy them into concrete sets.
        builtinContainerAttributes = false,
        typeImmutable = "ImmutableSetMembershipTransformationResult")
    interface Result<T> {
        @Value.Parameter(order = 0)
        Set<T> members();
        @Value.Parameter(order = 1)
        @Value.Default
        default Set<T> excludedAliens() {
            return ImmutableSet.of();
        }
        @Value.Parameter(order = 2)
        @Value.Default
        default Set<T> includedAliens() {
            return ImmutableSet.of();
        }
    }
}
