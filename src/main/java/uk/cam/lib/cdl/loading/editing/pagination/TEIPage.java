package uk.cam.lib.cdl.loading.editing.pagination;

import org.immutables.value.Value;

import java.util.Set;

/***
 * A TEIPage represents the additional TEI-specific data required when inserting
 * {@link Page Pages} into TEI XML.
 */
@Value.Immutable
public interface TEIPage {
    /** The actual page data. */
    Page page();

    /**
     * A unique identifier for the page, used when generating {@code xml:id}
     * attributes.
     */
    String identifier();

    /**
     * Semantic tags for the page, which are stored in a {@code decls} attribute
     * of the {@code <graphic>} element representing the page's image.
     */
    Set<String> tags();
}
