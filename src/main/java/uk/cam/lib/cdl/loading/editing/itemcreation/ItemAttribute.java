package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface ItemAttribute<T> {
    @Value.Parameter(order = 0)
    Type type();
    @Value.Parameter(order = 1)
    T value();

    /** Marker interface for types acting as {@link ItemAttribute#type()} */
    interface Type {
        default <T> ItemAttribute<T> containing(T value) {
            return ImmutableItemAttribute.of(this, value);
        }
    }
}
