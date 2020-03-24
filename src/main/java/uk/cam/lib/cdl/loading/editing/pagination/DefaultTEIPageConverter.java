package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@inheritDoc}
 *
 * <p>This implementation generates TEI Pages numeric identifiers counting
 * up from 1, and assigns each page a fixed list of tags.
 */
public class DefaultTEIPageConverter implements TEIPageConverter {
    private final List<String> tags;

    public DefaultTEIPageConverter(Iterable<String> tags) {
        Preconditions.checkNotNull(tags, "tags cannot be null");
        this.tags = ImmutableList.copyOf(tags);
    }

    @Override
    public List<TEIPage> convert(Iterable<? extends Page> pages) {
        return Streams.zip(Stream.iterate(0, i -> i + 1), Streams.stream(pages), (i, page) ->
            ImmutableTEIPage.builder()
                .page(page)
                .identifier(String.format("%d", i + 1))
                .addAllTags(this.tags)
                .build()
        ).collect(ImmutableList.toImmutableList());
    }
}
