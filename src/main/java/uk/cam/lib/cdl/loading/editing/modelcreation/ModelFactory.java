package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.Set;

public interface ModelFactory<T> {
    CreationResult<T> createFromAttributes(Set<? extends ModelAttribute<?>> modelAttributes) throws IOException;
    default CreationResult<T> createFromAttributes(ModelAttribute<?>... modelAttributes) throws IOException {
        return createFromAttributes(ImmutableSet.copyOf(modelAttributes));
    }
}
