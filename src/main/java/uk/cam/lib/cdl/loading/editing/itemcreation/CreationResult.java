package uk.cam.lib.cdl.loading.editing.itemcreation;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CreationResult<T> {
    boolean isSuccessful();
    Optional<T> value();
    Set<Issue> issues();

    <U> CreationResult<U> flatMap(Function<? super T, CreationResult<? extends U>> mapper);
    <U> CreationResult<U> map(Function<? super T, ? extends U> mapper);
    <U, V> CreationResult<V> flatBiMap(CreationResult<U> other, BiFunction<? super T, ? super U, CreationResult<? extends V>> mapper);
    <U, V> CreationResult<V> biMap(CreationResult<U> other, BiFunction<? super T, ? super U, ? extends V> mapper);
}
