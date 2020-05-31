package uk.cam.lib.cdl.loading.utils;

@FunctionalInterface
public interface ThrowingConsumer<Arg, Error extends Throwable> {
    void accept(Arg value) throws Error;

    default ThrowingFunction<Arg, Void, Error> asVoidFunction() {
        return value -> { accept(value); return null; };
    }
}
