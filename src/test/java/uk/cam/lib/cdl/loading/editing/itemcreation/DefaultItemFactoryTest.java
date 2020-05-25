package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class DefaultItemFactoryTest {

    public static class ImmutableInitialFileContentTest {
        public static final ItemAttribute<String> STRING_TEXT_ATTR =
            ItemAttributes.StandardItemAttributes.TEXT.containing("Foo");
        public static final ItemAttribute<CharSource> CHAR_SOURCE_TEXT_ATTR =
            ItemAttributes.StandardItemAttributes.TEXT.containing(CharSource.wrap("Foo"));
        public static final ItemAttribute<ByteSource> BYTES_ATTR =
            ItemAttributes.StandardItemAttributes.BYTES.containing(ByteSource.wrap(new byte[]{'F', 'o', 'o'}));
        public static final ItemAttribute<String> MIME_TYPE_ATTR =
            ItemAttributes.StandardItemAttributes.MIME_TYPE.containing("text/plain");
        public static final ItemAttribute<String> STRING_CHARSET_ATTR =
            ItemAttributes.StandardItemAttributes.CHARSET.containing("UTF-8");
        public static final ItemAttribute<Charset> CHARSET_ATTR =
            ItemAttributes.StandardItemAttributes.CHARSET.containing(StandardCharsets.UTF_8);

        public static final Set<ItemAttribute<?>> CHARSET_ATTRS =
            ImmutableSet.of(CHARSET_ATTR, STRING_CHARSET_ATTR);

        public static final Set<ItemAttribute<?>> TEXT_ATTRS =
            ImmutableSet.of(STRING_TEXT_ATTR, CHAR_SOURCE_TEXT_ATTR);

        public static final Set<ItemAttribute<?>> FILE_WITH_BYTES =
            ImmutableSet.of(BYTES_ATTR);

        public static final Set<Set<ItemAttribute<?>>> FILES_WITH_BYTES_AND_CHARSET = ImmutableSet.of(
            ImmutableSet.of(BYTES_ATTR, STRING_CHARSET_ATTR),
            ImmutableSet.of(BYTES_ATTR, CHARSET_ATTR)
        );

        public static final Set<Set<ItemAttribute<?>>> FILES_WITH_TEXT = ImmutableSet.of(
            ImmutableSet.of(STRING_TEXT_ATTR),
            ImmutableSet.of(CHAR_SOURCE_TEXT_ATTR)
        );

        public static final Set<Set<ItemAttribute<?>>> FILES_WITH_TEXT_AND_CHARSET =
            TEXT_ATTRS.stream()
                .flatMap(textAttr -> CHARSET_ATTRS.stream()
                    .map(charsetAttr -> ImmutableSet.of(textAttr, charsetAttr)))
            .collect(toImmutableSet());
    }
}
