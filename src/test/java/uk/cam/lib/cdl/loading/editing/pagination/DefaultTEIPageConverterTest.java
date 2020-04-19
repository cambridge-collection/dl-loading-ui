package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class DefaultTEIPageConverterTest {
    @Test
    public void convertConvertsPages() {
        var pages = ImmutableList.of(
            ImmutablePage.of("a", URI.create("/a")),
            ImmutablePage.of("b", URI.create("/b"))
        );

        var teiPages = new DefaultTEIPageConverter(ImmutableList.of("foo", "bar")).convert(pages);

        Truth.assertThat(pages).hasSize(2);
        for(int i = 0; i < pages.size(); ++i) {
            var page = pages.get(i);
            var teiPage = teiPages.get(i);

            Truth.assertThat(teiPage.page()).isSameInstanceAs(page);
            Truth.assertThat(teiPage.identifier()).isEqualTo("" + (i + 1));
            Truth.assertThat(teiPage.tags()).containsExactly("foo", "bar");
        }
    }
}
