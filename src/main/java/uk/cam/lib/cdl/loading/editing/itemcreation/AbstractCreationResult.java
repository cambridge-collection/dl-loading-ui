package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class AbstractCreationResult<T> implements CreationResult<T> {

    public static <T> ImmutableCreationResult<T> successful(T value) {
        return ImmutableCreationResult.<T>builder().isSuccessful(true).value(Optional.of(value)).build();
    }

    public static <T> ImmutableCreationResult<T> unsuccessful(Iterable<? extends Issue> issues) {
        return ImmutableCreationResult.<T>builder().isSuccessful(false).issues(issues).build();
    }

    public static <T> ImmutableCreationResult<T> unsuccessful(Issue issue, Issue... more) {
        return ImmutableCreationResult.<T>builder().isSuccessful(false).addIssues(issue).addIssues(more).build();
    }

    @Override
    public abstract boolean isSuccessful();
    @Override
    public abstract Optional<T> value();
    @Override
    public abstract Set<Issue> issues();

    @Value.Check
    protected void checkState() {
        if(isSuccessful()) {
            Preconditions.checkState(value().isPresent(), "successful CreationResults must have a value");
            Preconditions.checkState(issues().isEmpty(), "successful CreationResults must have no issues");
        }
        else {
            Preconditions.checkState(value().isEmpty(), "unsuccessful CreationResults must have no value");
            Preconditions.checkState(!issues().isEmpty(), "unsuccessful CreationResults must have at least one issue");
        }
    }
}
