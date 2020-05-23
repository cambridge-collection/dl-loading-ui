package uk.cam.lib.cdl.loading.model.editor.modelops;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(of = "ensure")
public abstract class AbstractModelState<T> implements ModelState<T> {
    @Value.Parameter(order = 0)
    public abstract ModelState.Ensure ensure();
    @Value.Parameter(order = 1)
    public abstract Class<T> type();
    @Value.Parameter(order = 2)
    public abstract T model();

    public static <T> ImmutableModelState<T> ensure(ModelState.Ensure ensure, T model) {
        // type could be a subtype of T, but it's safe to treat it as T as we're
        // immutable.
        // E.g. an ImmutableModelState<Number> reference could contain an Integer
        // and have Integer.class as its type, but there's no put(Number n)
        // method which would violate the real Integer type.
        @SuppressWarnings("unchecked")
        var type = (Class<T>)model.getClass();
        return ImmutableModelState.ensure(ensure, type, model);
    }

    public static <T> ImmutableModelState<T> ensurePresent(T model) {
        return ImmutableModelState.ensure(Ensure.PRESENT, model);
    }

    public static <T> ImmutableModelState<T> ensureAbsent(T model) {
        return ImmutableModelState.ensure(Ensure.ABSENT, model);
    }
}
