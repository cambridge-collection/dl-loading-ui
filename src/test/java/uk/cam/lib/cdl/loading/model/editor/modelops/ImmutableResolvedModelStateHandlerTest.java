package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.google.common.base.Preconditions;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class ImmutableResolvedModelStateHandlerTest {
    @Mock private ThrowingFunction<ModelState<?>, Object, IOException> handlerFunc;

    private ImmutableModelState<Integer> state;
    private ModelStateHandler<Object, Object> handler;
    private ImmutableResolvedModelStateHandler<Object, Object> resolvedHandler;

    @BeforeEach
    private void beforeEach() {
        Preconditions.checkNotNull(handlerFunc);
        state = ImmutableModelState.ensure(ModelState.Ensure.PRESENT, 42);
        handler = DefaultModelStateHandler.of(Object.class, handlerFunc);
        resolvedHandler = ImmutableResolvedModelStateHandler.of(state, handler);
    }

    @Test
    public void applyInvokesHandlerOnState() throws IOException {
        Mockito.when(handlerFunc.apply(Mockito.any())).thenReturn("42");
        Truth.assertThat(resolvedHandler.apply()).isEqualTo("42");
        Mockito.verify(handlerFunc, Mockito.times(1)).apply(state);
    }

    @Test
    public void applyDirectlyThrowsExceptionFromHandler() throws IOException {
        var err = new IOException("foo");
        Mockito.when(handlerFunc.apply(Mockito.any())).thenThrow(err);
        var thrown = Assertions.assertThrows(IOException.class, resolvedHandler::apply);
        Truth.assertThat(thrown).isSameInstanceAs(err);
    }
}
