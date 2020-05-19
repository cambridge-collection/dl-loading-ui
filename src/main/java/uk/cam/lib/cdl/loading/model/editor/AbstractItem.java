package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.nio.file.Path;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
abstract class AbstractItem implements Item {
    @Override
    @Value.Parameter(order = 0)
    public abstract Path id();

    @Value.Check
    protected void validate() {
        ModelOps().validatePathForId(id());
        fileData().ifPresent(fileData -> Preconditions.checkNotNull(fileData, "fileData cannot contain null"));
    }

    public static ImmutableItem of(Path id, String fileData) {
        return ImmutableItem.of(id).withFileData(fileData);
    }
}
