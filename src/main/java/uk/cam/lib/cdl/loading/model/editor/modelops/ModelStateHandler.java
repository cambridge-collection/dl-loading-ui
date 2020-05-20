package uk.cam.lib.cdl.loading.model.editor.modelops;

import java.io.IOException;
import java.util.Optional;

public interface ModelStateHandler<T> {
    <X> Optional<ModelStateHandler<? super X>> match(ModelState<X> state);
    void handle(ModelState<? extends T> state) throws IOException;
}
