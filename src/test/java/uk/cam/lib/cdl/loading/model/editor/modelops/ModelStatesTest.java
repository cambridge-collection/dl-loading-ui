package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.testutils.Models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.ABSENT;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

@ExtendWith(MockitoExtension.class)
public class ModelStatesTest {
    private static final ModelState<String> INVALID_TYPE_STATE = ImmutableModelState.ensure(PRESENT, "foo");

    @Mock
    private ModelOps mockModelOps;
    private final Path dataRoot = Path.of("/foo/bar");

    private <T> void assertInvalidStatesAreRejected(ModelStateHandler<T> handler, Iterable<ModelState<?>> invalidStates) {
        @SuppressWarnings("unchecked")
        var misTypedHandler = (ModelStateHandler<Object>) handler;
        for(var invalidState : invalidStates) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> misTypedHandler.handle(invalidState));
            Mockito.verifyNoMoreInteractions(mockModelOps);
        }
    }

    @Test
    public void itemWriter() throws IOException {
        var itemWriter = ModelStates.itemWriter(mockModelOps, dataRoot);
        var validState = ImmutableModelState.ensure(PRESENT, ImmutableItem.of(Path.of("foo")));
        var invalidStates = ImmutableList.of(validState.withEnsure(ABSENT), INVALID_TYPE_STATE);

        itemWriter.handle(validState);
        Mockito.verify(mockModelOps).writeItem(dataRoot, validState.model());
        Mockito.verifyNoMoreInteractions(mockModelOps);

        assertInvalidStatesAreRejected(itemWriter, invalidStates);
    }

    @Test
    public void itemRemover() throws IOException {
        var itemRemover = ModelStates.itemRemover(mockModelOps, dataRoot);
        var validState = ImmutableModelState.ensure(ABSENT, ImmutableItem.of(Path.of("foo")));
        var invalidStates = ImmutableList.of(validState.withEnsure(PRESENT), INVALID_TYPE_STATE);

        itemRemover.handle(validState);
        Mockito.verify(mockModelOps).removeItem(dataRoot, validState.model());
        Mockito.verifyNoMoreInteractions(mockModelOps);

        assertInvalidStatesAreRejected(itemRemover, invalidStates);
    }

    @Test
    public void collectionWriter() throws IOException {
        var mapper = Mockito.mock(ObjectMapper.class);
        var collectionWriter = ModelStates.collectionWriter(mockModelOps, mapper, dataRoot);
        var validState = ImmutableModelState.ensure(PRESENT, Models.exampleCollection(Path.of("foo")));
        var invalidStates = ImmutableList.<ModelState<?>>of(validState.withEnsure(ABSENT), INVALID_TYPE_STATE);

        collectionWriter.handle(validState);
        Mockito.verify(mockModelOps).writeCollectionJson(mapper, dataRoot, validState.model());
        Mockito.verifyNoMoreInteractions(mockModelOps);

        assertInvalidStatesAreRejected(collectionWriter, invalidStates);
    }

    @Test
    public void collectionRemover() throws IOException {
        var collectionRemover = ModelStates.collectionRemover(mockModelOps, dataRoot);
        var validState = ImmutableModelState.ensure(ABSENT, Models.exampleCollection(Path.of("foo")));
        var invalidStates = ImmutableList.<ModelState<?>>of(validState.withEnsure(PRESENT), INVALID_TYPE_STATE);

        collectionRemover.handle(validState);
        Mockito.verify(mockModelOps).removeCollection(dataRoot, validState.model());
        Mockito.verifyNoMoreInteractions(mockModelOps);

        assertInvalidStatesAreRejected(collectionRemover, invalidStates);
    }
}
