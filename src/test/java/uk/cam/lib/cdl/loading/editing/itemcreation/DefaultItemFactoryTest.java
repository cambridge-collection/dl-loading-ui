package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.cam.lib.cdl.loading.editing.itemcreation.ItemAttributes.StandardItemAttributes;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class DefaultItemFactoryTest {



    public static class ImmutableInitialFileContentTest {
        private static ImmutableInitialFileContent newIIFC(ItemAttribute<?>...attributes) {
            return ImmutableInitialFileContent.builder().addAttributes(attributes).build();
        }

        @Test
        public void ifc_containingBytesWithNoCharset() throws IOException {
            var ifc = newIIFC(StandardItemAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes())));

            assertThat(ifc.text()).isEmpty();
            assertThat(ifc.bytes().orElseThrow().contentEquals(ByteSource.wrap("foo".getBytes()))).isTrue();
        }

        @Test
        public void ifc_containingBytesWithCharset() throws IOException {
            var ifc = newIIFC(
                StandardItemAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes())),
                StandardItemAttributes.CHARSET.containing("UTF-8")
            );

            assertThat(ifc.text().orElseThrow().read()).isEqualTo("foo");
            assertThat(ifc.bytes().orElseThrow().contentEquals(ByteSource.wrap("foo".getBytes()))).isTrue();
        }

        @Test
        public void ifc_containingTextWithCharset() throws IOException {
            var ifc = newIIFC(
                StandardItemAttributes.TEXT.containing(CharSource.wrap("foo")),
                StandardItemAttributes.CHARSET.containing("UTF-8")
            );

            assertThat(ifc.text().orElseThrow().read()).isEqualTo("foo");
            assertThat(ifc.bytes().orElseThrow().contentEquals(ByteSource.wrap("foo".getBytes()))).isTrue();
        }

        @Test
        public void ifc_containingTextAndBytes_isInvalid() {
            Assertions.assertThrows(IllegalStateException.class, () -> newIIFC(
                StandardItemAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes())),
                StandardItemAttributes.TEXT.containing("foo")
            ));
        }

        @Test
        public void ifc_containingNoAttributes_isInvalid() {
            Assertions.assertThrows(IllegalStateException.class, () -> newIIFC());
        }
    }
}
