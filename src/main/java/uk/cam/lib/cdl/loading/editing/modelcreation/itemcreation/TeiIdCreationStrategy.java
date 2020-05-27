package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultModelFactory;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableCreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public abstract class TeiIdCreationStrategy implements DefaultModelFactory.IdCreationStrategy {
    public static TeiIdCreationStrategyBuilder builder() {
        return new TeiIdCreationStrategyBuilder();
    }
    public static TeiIdCreationStrategy of() {
        return builder().build();
    }
    public static TeiIdCreationStrategy of(Path baseTeiItemPath) {
        return builder().baseTeiItemPath(baseTeiItemPath).build();
    }

    public static final Pattern FILENAME_PATTERN = Pattern.compile(
        "^([a-z0-9]+-(?:[a-z0-9]+-)+[0-9]{5})\\.xml$", Pattern.CASE_INSENSITIVE);

    public static final Path DEFAULT_BASE_TEI_ITEM_PATH = Path.of("data/items/data/tei");

    @Value.Default
    public Path baseTeiItemPath() {
        return DEFAULT_BASE_TEI_ITEM_PATH;
    }

    @Value.Check
    protected void checkState() {
        ModelOps.ModelOps().validatePathForId(baseTeiItemPath());
    }

    @Override
    public CreationResult<Path> createId(Set<? extends ModelAttribute<?>> modelAttributes) {
        var filename = ModelAttributes.requireAttribute(ModelAttributes.StandardFileAttributes.FILENAME, String.class, modelAttributes)
            .value();

        var matcher = FILENAME_PATTERN.matcher(filename);
        if(!matcher.matches()) {
            return ImmutableCreationResult.unsuccessful(ImmutableIssue.of(Issue.INVALID_FILENAME,
                String.format(
                    "Item name '%s' not valid. Should be for example: 'MS-TEST-00001.xml' " +
                    "Using characters A-Z or numbers 0-9 and the - character delimiting sections. " +
                    "Should have at least 3 sections, group (MS = Manuscripts, PR = printed etc) then " +
                    "the collection, then a five digit number.", filename)));
        }

        var name = matcher.group(1);

        var id = baseTeiItemPath().resolve(Path.of(name, name + ".xml"));
        return ImmutableCreationResult.successful(ModelOps.ModelOps().validatePathForId(id));
    }

    public enum Issue implements uk.cam.lib.cdl.loading.editing.modelcreation.Issue.Type {
        INVALID_FILENAME
    }
}
