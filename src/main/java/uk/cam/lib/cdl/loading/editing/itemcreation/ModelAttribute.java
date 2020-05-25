package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

@Value.Immutable
public interface ModelAttribute<T> {
    @Value.Parameter(order = 0)
    Type type();
    @Value.Parameter(order = 1)
    T value();

    /** Marker interface for types acting as {@link ModelAttribute#type()} */
    interface Type {
        default <T> ModelAttribute<T> containing(T value) {
            return ImmutableModelAttribute.of(this, value);
        }
    }
}
