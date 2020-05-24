package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
public interface CreationResult<T> {
    boolean isSuccessful();
    Optional<T> value();
    Set<Issue> issues();
}
