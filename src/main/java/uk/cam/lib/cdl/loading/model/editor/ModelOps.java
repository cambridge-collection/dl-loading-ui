package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import com.google.common.io.ByteStreams;
import org.immutables.value.Value;

import javax.validation.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Value.Immutable(singleton = true)
@Value.Style(
    typeImmutable = "Default*",
    visibility = Value.Style.ImplementationVisibility.PACKAGE)
public interface ModelOps {
    static ModelOps ModelOps() {
        return DefaultModelOps.of();
    }

    default String itemMetadataAsString(Item item) throws IOException {
        return Files.readString(item.getFilepath());
    }

    default boolean pathIsNormalised(Path path) {
        Preconditions.checkNotNull(path);
        return Streams.stream(path).map(Object::toString).noneMatch(segment -> ".".equals(segment) || "..".equals(segment));
    }

    default Path validatePathForId(Path path) {
        Preconditions.checkState(!path.isAbsolute(), "path '%s' is not a valid ID: path is absolute", path);
        Preconditions.checkState(pathIsNormalised(path), "path '%s' is not a valid ID: path is not normalised", path);
        Preconditions.checkState(!path.toString().equals(""), "path '%s' is not a valid ID: path is empty", path);
        return path;
    }

    default Path validateSubpath(Path path) {
        Preconditions.checkState(!path.isAbsolute(), "path '%s' is not a valid sub path: path is absolute", path);
        Preconditions.checkState(pathIsNormalised(path), "path '%s' is not a valid sub path: path is not normalised", path);
        Preconditions.checkState(!path.toString().equals(""), "path '%s' is not a valid sub path: path is empty", path);
        return path;
    }

    default Path validatePathForIO(Path path) {
        Preconditions.checkState(path.isAbsolute(), "path '%s' is not valid for IO: path is not absolute", path);
        // This isn't necessary really, but a non-normalised path is probably a sign of mishandling
        Preconditions.checkState(pathIsNormalised(path), "path '%s' is not valid for IO: path is not normalised", path);
        return path;
    }

    default Path resolveIdToIOPath(Path dataRoot, Path id) {
        validatePathForIO(dataRoot);
        validatePathForId(id);
        // No need to normalise as both paths already are normalised
        return validatePathForIO(dataRoot.resolve(id));
    }

    default Path resolveReferenceAsId(Path contextFileId, Path reference) {
        validatePathForId(contextFileId);
        return validatePathForId(contextFileId.resolveSibling(reference).normalize());
    }

    /**
     * Construct a relative reference to an id from a context id.
     *
     * @return A path to reference the id with from the context file.
     */
    default Path relativizeIdAsReference(Path contextFileId, Path id) {
        validatePathForId(contextFileId);
        validatePathForId(id);
        return contextFileId.getParent().relativize(id);
    }

    default Stream<Path> streamResolvedItemIds(Collection collection) {
        Path collectionPath = Path.of(collection.getCollectionId());
        validatePathForId(collectionPath);

        return collection.getItemIds().stream()
            .map(id -> resolveReferenceAsId(collectionPath, Path.of(id.getId())));
    }

    default boolean isItemInCollection(Item item, Collection collection) {
        return isItemInCollection(Path.of(item.getId().getId()), collection);
    }
    default boolean isItemInCollection(Path itemId, Collection collection) {
        validatePathForId(itemId);
        return streamResolvedItemIds(collection).anyMatch(itemId::equals);
    }
}
