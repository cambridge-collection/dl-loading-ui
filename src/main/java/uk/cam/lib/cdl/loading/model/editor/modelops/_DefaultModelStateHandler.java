package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Value.Style(typeAbstract = "_*", typeImmutable = "*", depluralize = true)
abstract class  _DefaultModelStateHandler<T> implements ModelStateHandler<T> {
    private static final Set<ModelState.Ensure> ALL_ENSURE_VALUES =
        Sets.immutableEnumSet(Arrays.asList(ModelState.Ensure.values()));

    /**
     * Construct a handler which accepts only the specified ensure value.
     */
    public static <T> DefaultModelStateHandler<T> of(Class<T> type, ModelState.Ensure ensure, ThrowingConsumer<? super ModelState<? extends T>, IOException> handler) {
        return DefaultModelStateHandler.<T>builder()
            .type(type)
            .addSupportedEnsureValue(ensure)
            .handler(handler)
            .build();
    }

    @Value.Parameter(order = 0)
    public abstract Class<T> type();

    @Value.Parameter(order = 1)
    protected abstract ThrowingConsumer<? super ModelState<? extends T>, IOException> handler();

    @Value.Default
    public Set<ModelState.Ensure> supportedEnsureValues() {
        return ALL_ENSURE_VALUES;
    }

    @Override
    public <X> Optional<ModelStateHandler<? super X>> match(ModelState<X> state) {
        Preconditions.checkNotNull(state);
        if(!supportedEnsureValues().contains(state.ensure())) {
            return Optional.empty();
        }

        if(type().isAssignableFrom(state.type())) {
            @SuppressWarnings("unchecked")
            var _this = (ModelStateHandler<? super X>)this;
            return Optional.of(_this);
        }
        return Optional.empty();
    }

    @Override
    public void handle(ModelState<? extends T> state) throws IOException {
        Preconditions.checkNotNull(state);
        Preconditions.checkArgument(supportedEnsureValues().contains(state.ensure()),
            "unsupported Ensure value: %s, supported values: %s",
            state.ensure(), supportedEnsureValues());
        Preconditions.checkArgument(type().isAssignableFrom(state.type()),
            "State has incorrect type. State's type %s is not compatible with our type: %s", state.type(), type());
        handler().accept(state);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Throwable> {
        void accept(T value) throws E;
    }
}
