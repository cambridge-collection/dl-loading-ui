package uk.cam.lib.cdl.loading.editing.pagination;

import java.util.List;

/**
 * Encapsulates the creation of TEI-specific {@link TEIPage} objects from
 * simpler, generic {@link Page Pages}.
 */
public interface TEIPageConverter {
    List<TEIPage> convert(Iterable<? extends Page> pages);
}
