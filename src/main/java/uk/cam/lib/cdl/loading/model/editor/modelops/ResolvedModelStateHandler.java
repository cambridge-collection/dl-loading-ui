package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.immutables.value.Value;

import java.io.IOException;

@Value.Immutable
@Value.Style(typeImmutable = "Immutable*")
public
interface ResolvedModelStateHandler<T> {
    @Value.Parameter(order = 0)
    ModelState<? extends T> state();
    @Value.Parameter(order = 1)
    ModelStateHandler<? super T> handler();
    default void apply() throws IOException {
        handler().handle(state());
    }
}
