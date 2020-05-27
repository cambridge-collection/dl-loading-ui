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

    private static final <T> CreationResult<T> unsuccessfulResult() {
        return ImmutableCreationResult.unsuccessful(
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo1."),
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo2."),
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo3."));
    }

    @Test
    public void map_successful() {
        assertThat(ImmutableCreationResult.successful(42).mapValue(value -> value + 1))
            .isEqualTo(ImmutableCreationResult.successful(43));
    }

    @Test
    public void map_unsuccessful() {
        CreationResult<String> a = unsuccessfulResult();
        CreationResult<Integer> b = a.mapValue(Integer::parseInt);

        assertThat(b.isSuccessful()).isFalse();
        assertThat(b).isSameInstanceAs(a);
    }

    @Test
    public void flatmap() {
        CreationResult<String> unsuccessful = unsuccessfulResult();
        CreationResult<String> successful = ImmutableCreationResult.successful("42");

        assertThat(unsuccessful.flatMapValue(value -> ImmutableCreationResult.successful(Integer.parseInt(value))))
            .isSameInstanceAs(unsuccessful);
        assertThat(successful.flatMapValue(value -> ImmutableCreationResult.successful(Integer.parseInt(value))))
            .isEqualTo(ImmutableCreationResult.successful(42));
        assertThat(successful.flatMapValue(value -> ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2)))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2));
    }

    private static final Issue ISSUE1 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo1.");
    private static final Issue ISSUE2 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo2.");
    private static final Issue ISSUE3 = ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Foo3.");

    @Test
    public void biMap() {
        CreationResult<String> unsuccessfulA = ImmutableCreationResult.unsuccessful(ISSUE1);
        CreationResult<Integer> unsuccessfulB = ImmutableCreationResult.unsuccessful(ISSUE2, ISSUE3);
        CreationResult<String> successfulA = ImmutableCreationResult.successful("1");
        CreationResult<Integer> successfulB = ImmutableCreationResult.successful(2);

        assertThat(unsuccessfulA.biMapValue(unsuccessfulB, (String a, Integer b) -> a + ":" + b))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2, ISSUE3));
        assertThat(unsuccessfulA.biMapValue(successfulB, (String a, Integer b) -> a + ":" + b))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1));
        assertThat(successfulA.biMapValue(unsuccessfulB, (String a, Integer b) -> a + ":" + b))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE2, ISSUE3));
        assertThat(successfulA.biMapValue(successfulB, (String a, Integer b) -> a + ":" + b))
            .isEqualTo(ImmutableCreationResult.successful("1:2"));
    }

    @Test
    public void flatBiMap() {
        CreationResult<String> unsuccessfulA = ImmutableCreationResult.unsuccessful(ISSUE1);
        CreationResult<Integer> unsuccessfulB = ImmutableCreationResult.unsuccessful(ISSUE2, ISSUE3);
        CreationResult<String> successfulA = ImmutableCreationResult.successful("1");
        CreationResult<Integer> successfulB = ImmutableCreationResult.successful(2);

        assertThat(unsuccessfulA.flatBiMapValue(unsuccessfulB, (String a, Integer b) ->
            ImmutableCreationResult.successful(a + ":" + b)))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2, ISSUE3));
        assertThat(unsuccessfulA.flatBiMapValue(successfulB, (String a, Integer b) ->
            ImmutableCreationResult.successful(a + ":" + b)))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1));
        assertThat(successfulA.flatBiMapValue(unsuccessfulB, (String a, Integer b) ->
            ImmutableCreationResult.successful(a + ":" + b)))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE2, ISSUE3));
        assertThat(successfulA.flatBiMapValue(successfulB, (String a, Integer b) ->
            ImmutableCreationResult.successful(a + ":" + b)))
            .isEqualTo(ImmutableCreationResult.successful("1:2"));
        assertThat(successfulA.flatBiMapValue(successfulB, (String a, Integer b) ->
            ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2)))
            .isEqualTo(ImmutableCreationResult.unsuccessful(ISSUE1, ISSUE2));
    }


    public enum ExampleIssueType implements Issue.Type {
        EXAMPLE
    }
}
