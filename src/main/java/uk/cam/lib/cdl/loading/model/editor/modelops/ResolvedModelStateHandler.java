package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.immutables.value.Value;

import java.io.IOException;

@Value.Immutable
public interface ResolvedModelStateHandler<T, R> {
    @Value.Parameter(order = 0)
    ModelState<? extends T> state();
    @Value.Parameter(order = 1)
    ModelStateHandler<? super T, ? extends R> handler();
    default R apply() throws IOException {
        return handler().handle(state());
    }
}
