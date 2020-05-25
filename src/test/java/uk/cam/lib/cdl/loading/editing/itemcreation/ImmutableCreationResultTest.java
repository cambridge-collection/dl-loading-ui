package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class ImmutableCreationResultTest {
    @Test
    public void successful() {
        var result = ImmutableCreationResult.successful(42);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.value()).hasValue(42);
        assertThat(result.issues()).isEmpty();
    }

    @Test
    public void unsuccessful_viaVarargsSingle() {
        var issue = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo.");
        var result = ImmutableCreationResult.unsuccessful(issue);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.value()).isEmpty();
        assertThat(result.issues()).containsExactly(issue);
    }

    @Test
    public void unsuccessful_viaVarargsMultiple() {
        var issue1 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo1.");
        var issue2 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo2.");
        var issue3 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo3.");
        var result = ImmutableCreationResult.unsuccessful(issue1, issue2, issue3);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.value()).isEmpty();
        assertThat(result.issues()).containsExactly(issue1, issue2, issue3);
    }

    @Test
    public void unsuccessful_viaSet() {
        var issues = ImmutableSet.of(
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo1."),
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo2."),
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo3."));
        var result = ImmutableCreationResult.unsuccessful(issues);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.value()).isEmpty();
        assertThat(result.issues()).isEqualTo(issues);
    }

    public enum ExampleIssueType implements Issue.Type {
        EXAMPLE
    }
}
