package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

@Value.Immutable
public interface ItemAttribute<T> {
    Type type();
    T value();

    /** Marker interface for types acting as {@link ItemAttribute#type()} */
    interface Type {}
}
