package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@Value.Style(typeAbstract = "_*", typeImmutable = "*", depluralize = true,
    visibility = Value.Style.ImplementationVisibility.PUBLIC)
interface _DefaultModelStateHandlerResolver extends ModelStateHandlerResolver {
    List<ModelStateHandler<?>> handlers();

    @Override
    default <T> Optional<ResolvedModelStateHandler<? super T>> resolveHandler(ModelState<T> state) {
        return handlers().stream()
            .flatMap(handler -> bind(state, handler).stream())
            .findFirst();
    }

    static <S, H> Optional<ResolvedModelStateHandler<? super S>> bind(ModelState<S> state, ModelStateHandler<H> handler) {
        return handler.match(state).map(_handler -> ImmutableResolvedModelStateHandler.of(state, _handler));
    }
}
