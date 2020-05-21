package uk.cam.lib.cdl.loading.utils;

@FunctionalInterface
public interface ThrowingFunction<Arg, Result, Error extends Throwable> {
    Result apply(Arg value) throws Error;
}
