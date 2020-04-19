package uk.cam.lib.cdl.loading.editing.pagination;

import java.util.List;

/**
 * PageLoaders are responsible for generating pagination data from a source,
 * e.g. a CSV file.
 */
public interface PageLoader<T> {
    List<Page> loadPages(T source);
}
