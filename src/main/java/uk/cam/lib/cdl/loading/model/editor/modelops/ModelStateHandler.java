package uk.cam.lib.cdl.loading.model.editor.modelops;

import java.io.IOException;
import java.util.Optional;

public interface ModelStateHandler<T, R> {
    <X> Optional<ModelStateHandler<? super X, R>> match(ModelState<X> state);
    R handle(ModelState<? extends T> state) throws IOException;
}
