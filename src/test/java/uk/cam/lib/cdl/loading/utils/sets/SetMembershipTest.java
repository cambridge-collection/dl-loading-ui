package uk.cam.lib.cdl.loading.utils.sets;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

public class SetMembershipTest {
    @ParameterizedTest
    @MethodSource("membershipApplicationExamples")
    public <T> void membershipApplication(
        SetMembershipTransformation<T> membershipTransformation,
        Set<T> population,
        Set<T> universe,
        Object membershipOrError) {

        if(membershipOrError instanceof SetMembershipTransformation.Result) {
            Truth.assertThat(membershipTransformation.appliedTo(population, universe))
                .isEqualTo(membershipOrError);
        }
        else {
            var expectedErr = (Exception)membershipOrError;
            var err = Assertions.assertThrows(expectedErr.getClass(), () -> membershipTransformation.appliedTo(population, universe));
            Truth.assertThat(err).hasMessageThat().isEqualTo(expectedErr.getMessage());
        }
    }

    public static Stream<Arguments> membershipApplicationExamples() {
        return Stream.of(
            Arguments.of(
                SetMembership.onlyMemberOf(ImmutableSet.of(1)),
                ImmutableSet.of(1, 2),
                ImmutableSet.of(1, 2, 4, 5),
                ImmutableSetMembershipTransformationResult.builder()
                    .members(ImmutableSet.of(1))
                    .build()
            ),
            Arguments.of(
                SetMembership.onlyMemberOf(ImmutableSet.of(1, 6, 9)),
                ImmutableSet.of(1, 2),
                ImmutableSet.of(1, 2, 4, 5),
                ImmutableSetMembershipTransformationResult.builder()
                    .members(ImmutableSet.of(1))
                    .includedAliens(ImmutableSet.of(6, 9))
                    .build()
            ),
            Arguments.of(
                SetMembership.onlyMemberOf(ImmutableSet.of(1)),
                ImmutableSet.of(1, 2, 3, 4),
                ImmutableSet.of(1, 2),
                new IllegalArgumentException("members contains elements not present in universe: [3, 4]")
            ),
            Arguments.of(
                SetMembership.addingAndRemoving(ImmutableSet.of(3), ImmutableSet.of()),
                ImmutableSet.of(1, 2),
                ImmutableSet.of(1, 2, 3, 4, 5, 6, 7),
                ImmutableSetMembershipTransformationResult.builder()
                    .members(ImmutableSet.of(1, 2, 3))
                    .build()
            ),
            Arguments.of(
                SetMembership.addingAndRemoving(ImmutableSet.of(), ImmutableSet.of(2)),
                ImmutableSet.of(1, 2),
                ImmutableSet.of(1, 2, 3, 4, 5, 6, 7),
                ImmutableSetMembershipTransformationResult.builder()
                    .members(ImmutableSet.of(1))
                    .build()
            ),
            Arguments.of(
                SetMembership.addingAndRemoving(ImmutableSet.of(1, 7, 8), ImmutableSet.of(3, 9)),
                ImmutableSet.of(1, 2, 3, 4),
                ImmutableSet.of(1, 2, 3, 4, 5, 6, 7),
                ImmutableSetMembershipTransformationResult.builder()
                    .members(ImmutableSet.of(1, 2, 4, 7))
                    .includedAliens(ImmutableSet.of(8))
                    .excludedAliens(ImmutableSet.of(9))
                    .build()
            ),
            Arguments.of(
                SetMembership.addingAndRemoving(ImmutableSet.of(), ImmutableSet.of(2)),
                ImmutableSet.of(1, 2, 3, 4),
                ImmutableSet.of(1, 2),
                new IllegalArgumentException("members contains elements not present in universe: [3, 4]")
            )
        );
    }

    @Test
    public void addingAndRemoving_rejectsOverlappingAddAndRemove() {
        var exc = Assertions.assertThrows(IllegalArgumentException.class,
            () -> SetMembership.addingAndRemoving(ImmutableSet.of(2, 3, 7), ImmutableSet.of(2, 3, 4)));
        Truth.assertThat(exc).hasMessageThat().isEqualTo("excluded and included conflict - both contain: [2, 3]");
    }
}
