package uk.cam.lib.cdl.loading.editing.pagination;

import org.immutables.value.Value;

import java.net.URI;

/**
 * A Page contains the data for a single page of an item: Essentially the
 */
@Value.Immutable
public interface Page {
    /** The human-readable name for the page, e.g. 1r or 1v. */
    @Value.Parameter(order = 0)
    String label();

    /** The location or identifier of the Page's image.  */
    @Value.Parameter(order = 1)
    URI image();
}
