package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ModelStateEnforcementFailureException extends ModelOpsException {
    private final List<ModelStateEnforcementResult> results;

    public ModelStateEnforcementFailureException(
        String message, Throwable cause, List<ModelStateEnforcementResult> results) {
        super(message, cause);
        this.results = ImmutableList.copyOf(results);
    }

    public List<ModelStateEnforcementResult> results() {
        return results;
    }
}
