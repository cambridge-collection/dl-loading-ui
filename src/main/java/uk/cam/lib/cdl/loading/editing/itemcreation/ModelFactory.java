package uk.cam.lib.cdl.loading.editing.itemcreation;

import java.io.IOException;
import java.util.Set;

public interface ModelFactory<T> {
    CreationResult<T> createFromAttributes(Set<ModelAttribute<?>> modelAttributes) throws IOException;
}
