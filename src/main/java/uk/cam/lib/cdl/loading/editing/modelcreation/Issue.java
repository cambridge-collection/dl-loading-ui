package uk.cam.lib.cdl.loading.editing.modelcreation;

import org.immutables.value.Value;

@Value.Immutable
public interface Issue {
    @Value.Parameter(order = 0)
    Issue.Type type();
    @Value.Parameter(order = 1)
    String description();

    /** Marker interface for types acting as {@link Issue#type()} */
    interface Type {}
}
