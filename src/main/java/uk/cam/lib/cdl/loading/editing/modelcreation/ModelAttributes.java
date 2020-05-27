package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModelAttributes {
    private ModelAttributes() {}

    public static ModelAttribute<String> filename(String filename) {
        return ImmutableModelAttribute.of(StandardFileAttributes.FILENAME, filename);
    }

    public enum StandardFileAttributes implements ModelAttribute.Type {
        FILENAME,
        MIME_TYPE,
        CHARSET,
        BYTES,
        TEXT
    }

    public static <T> Stream<ModelAttribute<T>> streamAttributes(
            ModelAttribute.Type attributeType, Class<T> valueType, Stream<? extends ModelAttribute<?>> attributes) {
        Preconditions.checkNotNull(attributeType, "attributeType must not be null");
        Preconditions.checkNotNull(valueType, "valueType must not be null");
        Preconditions.checkNotNull(attributes, "attributes must not be null");

        return attributes.filter(matcher(attributeType, valueType))
            .map(attr -> {
                @SuppressWarnings("unchecked") var _attr = (ModelAttribute<T>)attr;
                return _attr;
            });
    }
    public static <T> Stream<ModelAttribute<T>> streamAttributes(
            ModelAttribute.Type attributeType, Class<T> valueType, Iterable<? extends ModelAttribute<?>> attributes) {
        return streamAttributes(attributeType, valueType, Streams.stream(attributes));
    }

    public static <T> Optional<ModelAttribute<T>> findAttribute(ModelAttribute.Type attributeType, Class<T> valueType, Iterable<? extends ModelAttribute<?>> attributes) {
        return streamAttributes(attributeType, valueType, attributes)
            .findFirst();
    }

    public static <T> ModelAttribute<T> requireAttribute(
            ModelAttribute.Type attributeType, Class<T> valueType, Iterable<? extends ModelAttribute<?>> attributes) {
        return findAttribute(attributeType, valueType, attributes)
            .orElseThrow(() -> attributeNotFoundException(attributeType, valueType, attributes));
    }

    public static AttributeNotFoundException attributeNotFoundException(ModelAttribute.Type attributeType, Class<?> valueType, Iterable<? extends ModelAttribute<?>> attributes) {
        return new AttributeNotFoundException(String.format(
            "No ItemAttribute with type() %s assignable to %s found amongst %s",
            attributeType, valueType, attributes));
    }

    public static Predicate<ModelAttribute<?>> matcher(ModelAttribute.Type attributeType, Class<?> valueType) {
        Preconditions.checkNotNull(attributeType);
        Preconditions.checkNotNull(valueType);
        return attr -> attributeType.equals(attr.type()) && (attr.value() != null && valueType.isAssignableFrom(attr.value().getClass()));
    }
}
