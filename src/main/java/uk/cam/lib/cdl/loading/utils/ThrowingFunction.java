package uk.cam.lib.cdl.loading.utils;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<Arg, Result, Error extends Throwable> {
    Result apply(Arg value) throws Error;

    static <A, B> Function<A, B> dangerouslyMakeUnchecked(ThrowingFunction<A, B, ? extends Throwable> throwingFunction) {
        return arg -> {
            try {
                return throwingFunction.apply(arg);
            } catch (Throwable throwable) {
                _ThrowingFunction.sneakyThrow(throwable);
                throw new AssertionError("unreachable");
            }
        };
    }
}

class _ThrowingFunction {
    private _ThrowingFunction() {}

    static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
