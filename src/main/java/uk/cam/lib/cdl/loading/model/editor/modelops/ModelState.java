package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;

import java.util.Optional;

public interface ModelState<T> {
    Ensure ensure();
    Class<T> type();
    T model();

    default <X> Optional<ModelState<X>> match(Class<X> type) {
        Preconditions.checkNotNull(type);
        if(type.isAssignableFrom(type())) {
            // Note that it's safe to return a ModelState<X> rather than ModelState<? extends X>
            // as ModelState instances are immutable.
            @SuppressWarnings("unchecked")
            var _this = (ModelState<X>)this;
            return Optional.of(_this);
        }
        return Optional.empty();
    }

    enum Ensure {
        PRESENT,
        ABSENT
    }
}
