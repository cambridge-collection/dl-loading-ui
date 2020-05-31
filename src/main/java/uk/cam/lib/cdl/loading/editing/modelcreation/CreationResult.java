package uk.cam.lib.cdl.loading.editing.modelcreation;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CreationResult<T> {
    boolean isSuccessful();
    Optional<T> value();
    Set<Issue> issues();

    <U> CreationResult<U> flatMapValue(Function<? super T, CreationResult<? extends U>> mapper);
    <U> CreationResult<U> mapValue(Function<? super T, ? extends U> mapper);
    <U, V> CreationResult<V> flatBiMapValue(CreationResult<U> other, BiFunction<? super T, ? super U, CreationResult<? extends V>> mapper);
    <U, V> CreationResult<V> biMapValue(CreationResult<U> other, BiFunction<? super T, ? super U, ? extends V> mapper);
}
