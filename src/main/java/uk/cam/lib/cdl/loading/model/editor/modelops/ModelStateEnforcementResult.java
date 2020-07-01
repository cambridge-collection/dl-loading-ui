package uk.cam.lib.cdl.loading.model.editor.modelops;

import java.util.Optional;

public interface ModelStateEnforcementResult {
    Outcome outcome();
    ModelState<?> state();
    Optional<ResolvedModelStateHandler<?, ?>> resolution();
    Optional<Object> handlerResult();
    Optional<ModelOpsException> error();

    enum Outcome { SUCCESSFUL, RESOLUTION_FAILED, HANDLER_FAILED }
}
