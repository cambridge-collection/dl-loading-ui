package uk.cam.lib.cdl.loading.utils;

@FunctionalInterface
public interface ThrowingSupplier<Result, Error extends Throwable> {
    Result get() throws Error;
}
