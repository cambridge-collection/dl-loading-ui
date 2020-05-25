package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemAttributes {
    private ItemAttributes() {}

    public static ItemAttribute<String> filename(String filename) {
        return ImmutableItemAttribute.of(StandardItemAttributes.FILENAME, filename);
    }

    public enum StandardItemAttributes implements ItemAttribute.Type {
        FILENAME,
        MIME_TYPE,
        CHARSET,
        BYTES,
        TEXT
    }

    public static <T> Stream<ItemAttribute<T>> streamAttributes(
        ItemAttribute.Type attributeType, Class<T> valueType, Stream<ItemAttribute<?>> attributes) {
        Preconditions.checkNotNull(attributeType, "attributeType must not be null");
        Preconditions.checkNotNull(valueType, "valueType must not be null");
        Preconditions.checkNotNull(attributes, "attributes must not be null");

        return attributes.filter(matcher(attributeType, valueType))
            .map(attr -> {
                @SuppressWarnings("unchecked") var _attr = (ItemAttribute<T>)attr;
                return _attr;
            });
    }
    public static <T> Stream<ItemAttribute<T>> streamAttributes(
        ItemAttribute.Type attributeType, Class<T> valueType, Iterable<ItemAttribute<?>> attributes) {
        return streamAttributes(attributeType, valueType, Streams.stream(attributes));
    }

    public static <T> Optional<ItemAttribute<T>> findAttribute(ItemAttribute.Type attributeType, Class<T> valueType, Iterable<ItemAttribute<?>> attributes) {
        return streamAttributes(attributeType, valueType, attributes)
            .findFirst();
    }

    public static <T> ItemAttribute<T> requireAttribute(
        ItemAttribute.Type attributeType, Class<T> valueType, Iterable<ItemAttribute<?>> attributes) {
        return findAttribute(attributeType, valueType, attributes)
            .orElseThrow(() -> attributeNotFoundException(attributeType, valueType, attributes));
    }

    public static AttributeNotFoundException attributeNotFoundException(ItemAttribute.Type attributeType, Class<?> valueType, Iterable<ItemAttribute<?>> attributes) {
        return new AttributeNotFoundException(String.format(
            "No ItemAttribute with type() %s assignable to %s found amongst %s",
            attributeType, valueType, attributes));
    }

    public static Predicate<ItemAttribute<?>> matcher(ItemAttribute.Type attributeType, Class<?> valueType) {
        Preconditions.checkNotNull(attributeType);
        Preconditions.checkNotNull(valueType);
        return attr -> attributeType.equals(attr.type()) && (attr.value() != null && valueType.isAssignableFrom(attr.value().getClass()));
    }
}
