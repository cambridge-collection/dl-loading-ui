package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.io.CharSource;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.utils.sets.SetMembershipTransformation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
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

    default String readItemMetadataAsString(Path dataRoot, Item item) throws IOException {
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(item);
        return readMetadataAsString(dataRoot, item.id());
    }

    default String readMetadataAsString(Path dataRoot, Path id) throws IOException {
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(id);
        return Files.readString(resolveIdToIOPath(dataRoot, id));
    }

    default void writeMetadata(Path dataRoot, Path id, InputStream metadata) throws IOException {
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(metadata);
        var destination = resolveIdToIOPath(dataRoot, id);
        Files.createDirectories(destination.getParent());
        Files.copy(metadata, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    default void writeMetadata(Path dataRoot, Path id, String metadata) throws IOException {
        writeMetadata(dataRoot, id, CharSource.wrap(metadata).asByteSource(Charsets.UTF_8).openBufferedStream());
    }

    default void writeItem(Path dataRoot, Item item) throws IOException {
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(item);
        Preconditions.checkArgument(item.fileData().isPresent(), "item has no file data: %s", item.id());
        writeMetadata(dataRoot, item.id(), item.fileData().get());
    }

    default boolean removeItem(Path dataRoot, Item item) throws IOException {
        return removeMetadata(dataRoot, item.id());
    }

    default boolean removeCollection(Path dataRoot, Collection collection) throws IOException {
        return removeMetadata(dataRoot, collection.getIdAsPath());
    }

    default boolean removeMetadata(Path dataRoot, Path id) throws IOException {
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(id);
        var wasRemoved = Files.deleteIfExists(resolveIdToIOPath(dataRoot, id));
        if(wasRemoved) {
            cleanEmptyDirectories(dataRoot, id);
        }
        return wasRemoved;
    }

    default void cleanEmptyDirectories(Path dataRoot, Path removedId) throws IOException {
        var dir = resolveIdToIOPath(dataRoot, removedId).getParent();
        assert dir.startsWith(dataRoot);
        while(!dir.equals(dataRoot)) {
            try {
                Files.delete(dir);
            }
            catch(DirectoryNotEmptyException e) {
                break;
            }
            dir = dir.getParent();
        }
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
        return isItemInCollection(item.id(), collection);
    }
    default boolean isItemInCollection(Path itemId, Collection collection) {
        validatePathForId(itemId);
        return streamResolvedItemIds(collection).anyMatch(itemId::equals);
    }

    default boolean addItemToCollection(Collection collection, Item item) {
        return addItemToCollection(collection, item.id());
    }

    default boolean addItemToCollection(Collection collection, Path itemId) {
        Preconditions.checkNotNull(collection);
        validatePathForId(itemId);
        if(isItemInCollection(itemId, collection)) {
            return false;
        }

        collection.getItemIds().add(new Id(relativizeIdAsReference(collection.getIdAsPath(), itemId).toString()));
        return true;
    }

    default boolean removeItemFromCollection(Collection collection, Item item) {
        return removeItemFromCollection(collection, item.id());
    }
    default boolean removeItemFromCollection(Collection collection, Path itemId) {
        Preconditions.checkNotNull(collection);
        validatePathForId(itemId);
        if(!isItemInCollection(itemId, collection)) {
            return false;
        }

        var wasRemoved = collection.getItemIds().remove(new Id(relativizeIdAsReference(collection.getIdAsPath(), itemId).toString()));
        assert wasRemoved;
        return true;
    }

    default void writeCollectionJson(ObjectMapper mapper, Path dataRoot, Collection collection) throws IOException {
        Preconditions.checkNotNull(mapper);
        Preconditions.checkNotNull(dataRoot);
        Preconditions.checkNotNull(collection);
        validatePathForIO(dataRoot);

        var destination = resolveIdToIOPath(dataRoot, collection.getIdAsPath());
        Files.createDirectories(destination.getParent());
        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(destination.toFile(), collection);
    }

    /**
     * Update the Collections an Item is a member of.
     *
     * <p>The specified Collection models are mutated to reflect the membership
     * specified by the {@code membershipTransformation}.
     *
     * @param item The item who's membership state is to be enforced.
     * @param collections The collection instances to enforce the state in.
     * @return The set of Collections which were modified by the operation.
     */
    default MembershipDelta<Collection> transformItemCollectionMembership(
        Item item,
        Iterable<Collection> collections,
        SetMembershipTransformation<Path> membershipTransformation) {

        var delta = calculateCollectionMembershipTransformationDelta(item, collections, membershipTransformation);
        delta.additions().forEach(col -> addItemToCollection(col, item));
        delta.removals().forEach(col -> removeItemFromCollection(col, item));
        return delta;
    }

    @Value.Immutable
    @Value.Style(builtinContainerAttributes = false)
    interface MembershipDelta<T> {
        @Value.Default default Set<T> additions() { return ImmutableSet.of(); }
        @Value.Default default Set<T> removals() { return ImmutableSet.of(); }
        @Value.Default default Set<T> membership() { return ImmutableSet.of(); }
    }

    default MembershipDelta<Collection> calculateCollectionMembershipTransformationDelta(
        Item item,
        Iterable<Collection> collections,
        SetMembershipTransformation<Path> membershipTransformation
    ) {
        var collectionsById = Streams.stream(collections)
            .collect(ImmutableMap.toImmutableMap(Collection::getIdAsPath, Function.identity()));

        var currentMembership = Streams.stream(collections)
            .filter(col -> isItemInCollection(item, col))
            .map(Collection::getIdAsPath)
            .collect(ImmutableSet.toImmutableSet());
        var newMembership = membershipTransformation.appliedTo(currentMembership, collectionsById.keySet());

        Preconditions.checkState(newMembership.excludedAliens().isEmpty(),
            "membershipTransformation dictates item be removed from non-existent collections: %s",
            newMembership.excludedAliens());
        Preconditions.checkState(newMembership.includedAliens().isEmpty(),
            "membershipTransformation dictates item be added to non-existent collections: %s",
            newMembership.includedAliens());

        var additions = Sets.difference(newMembership.members(), currentMembership);
        var removals = Sets.difference(currentMembership, newMembership.members());

        return ImmutableMembershipDelta.<Collection>builder()
            .additions(additions.stream().map(collectionsById::get).collect(ImmutableSet.toImmutableSet()))
            .removals(removals.stream().map(collectionsById::get).collect(ImmutableSet.toImmutableSet()))
            .membership(newMembership.members().stream().map(collectionsById::get).collect(ImmutableSet.toImmutableSet()))
            .build();
    }
}
