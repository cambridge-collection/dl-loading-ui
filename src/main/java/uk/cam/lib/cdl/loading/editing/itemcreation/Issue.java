package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

@Value.Immutable
public interface Issue {
    Issue.Type type();
    String description();

    /** Marker interface for types acting as {@link Issue#type()} */
    interface Type {}
}
