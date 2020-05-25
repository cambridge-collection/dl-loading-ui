package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    private static <T> CreationResult<T> safelyCastWildcard(CreationResult<? extends T> result) {
        @SuppressWarnings("unchecked")
        var cast = (CreationResult<T>) result;

        return result instanceof ImmutableCreationResult ? cast : ImmutableCreationResult.<T>builder().from(cast).build();
    }

    @Override
    public <U> CreationResult<U> flatMap(Function<? super T, CreationResult<? extends U>> mapper) {
        if(!this.isSuccessful()) {
            @SuppressWarnings("unchecked")
            var result = (CreationResult<U>)this;
            return result;
        }
        return safelyCastWildcard(mapper.apply(this.value().orElseThrow()));
    }

    @Override
    public <U> CreationResult<U> map(Function<? super T, ? extends U> mapper) {
        return safelyCastWildcard(flatMap(mapper.andThen(ImmutableCreationResult::successful)));
    }

    @Override
    public <U, V> CreationResult<V> flatBiMap(CreationResult<U> other, BiFunction<? super T, ? super U, CreationResult<? extends V>> mapper) {
        return flatBiMap(this, other, mapper);
    }

    @Override
    public <U, V> CreationResult<V> biMap(CreationResult<U> other, BiFunction<? super T, ? super U, ? extends V> mapper) {
        return flatBiMap(other, mapper.andThen(ImmutableCreationResult::successful));
    }

    private static <T, U, V> CreationResult<V> flatBiMap(
        CreationResult<T> left, CreationResult<U> right,
        BiFunction<? super T, ? super U, CreationResult<? extends V>> mapper
    ) {
        return safelyCastWildcard(left.value().flatMap(leftValue -> right.value().map(rightValue -> mapper.apply(leftValue, rightValue)))
            .orElseGet(() -> ImmutableCreationResult.unsuccessful(Sets.union(left.issues(), right.issues()))));
    }

//    @Override
//    public <U> CreationResult<U> flatmap(Function<T, CreationResult<U>> mapper) {
//        if(!this.isSuccessful()) {
//            @SuppressWarnings("unchecked")
//            var result = (CreationResult<U>)this;
//            return result;
//        }
//        return null;
//    }
}
