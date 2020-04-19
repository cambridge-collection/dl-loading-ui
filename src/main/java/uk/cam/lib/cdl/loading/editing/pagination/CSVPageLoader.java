package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.immutables.value.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

@Value.Immutable
public abstract class CSVPageLoader implements PageLoader<Reader> {
    interface CSVRowAccessor {
        String get(CSVRecord row);

        static CSVRowAccessor of(String column) {
            Preconditions.checkNotNull(column, "column cannot be null");
            return row -> {
                if(!row.isMapped(column)) {
                    throw new PaginationException(String.format("CSV row has no column named '%s'", column));
                }
                return row.get(column);
            };
        }
    }

    abstract CSVRowAccessor labelAccessor();
    abstract CSVRowAccessor imageAccessor();
    abstract CSVFormat csvFormat();

    @Override
    public List<Page> loadPages(Reader source) {
        Preconditions.checkNotNull(source, "source cannot be null");
        try {
            return loadPages(Streams.stream(csvFormat().parse(source)));
        }
        catch (IOException e) {
            throw new PaginationException(e);
        }
    }

    private List<Page> loadPages(Stream<CSVRecord> rows) {
        return rows.map(this::pageFromRow).collect(ImmutableList.toImmutableList());
    }

    private Page pageFromRow(CSVRecord record) {
        String label = labelAccessor().get(record);
        String image = imageAccessor().get(record);
        URI imageURI;
        try {
            imageURI = new URI(UriComponentsBuilder.fromUriString(image).toUriString());
        }
        catch (RuntimeException | URISyntaxException e) {
            throw new PaginationException(String.format("Image location is not a valid URL: '%s'", image));
        }
        return ImmutablePage.builder().label(label).image(imageURI).build();
    }
}
