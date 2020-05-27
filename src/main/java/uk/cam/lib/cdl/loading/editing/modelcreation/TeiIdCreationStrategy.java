package uk.cam.lib.cdl.loading.editing.modelcreation;

import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

public class TeiIdCreationStrategy implements DefaultModelFactory.IdCreationStrategy {
    public static final Pattern FILENAME_PATTERN = Pattern.compile(
        "^([a-z0-9]+-(?:[a-z0-9]+-)+[0-9]{5})\\.xml$", Pattern.CASE_INSENSITIVE);

    public static final Path BASE_TEI_ITEM_PATH = Path.of("data/items/data/tei");

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

        var id = BASE_TEI_ITEM_PATH.resolve(Path.of(name, name + ".xml"));
        return ImmutableCreationResult.successful(ModelOps.ModelOps().validatePathForId(id));
    }

    public enum Issue implements uk.cam.lib.cdl.loading.editing.modelcreation.Issue.Type {
        INVALID_FILENAME
    }
}
