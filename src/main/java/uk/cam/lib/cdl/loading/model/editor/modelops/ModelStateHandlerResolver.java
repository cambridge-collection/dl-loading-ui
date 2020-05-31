package uk.cam.lib.cdl.loading.model.editor.modelops;

import java.util.Optional;

/**
 * @see DefaultModelStateHandlerResolver
 */
public interface ModelStateHandlerResolver {
    <T> Optional<ResolvedModelStateHandler<? super T, ?>> resolveHandler(ModelState<T> state);
}
