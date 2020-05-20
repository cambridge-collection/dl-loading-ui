package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;

import java.util.Optional;

public interface ModelState<T> {
    Ensure ensure();
    Class<T> type();
    T model();

    default <X> Optional<ModelState<? extends X>> match(Class<X> type) {
        Preconditions.checkNotNull(type);
        if(type.isAssignableFrom(type())) {
            @SuppressWarnings("unchecked")
            var _this = (ModelState<? extends X>)this;
            return Optional.of(_this);
        }
        return Optional.empty();
    }

    enum Ensure {
        PRESENT,
        ABSENT
    }
}
