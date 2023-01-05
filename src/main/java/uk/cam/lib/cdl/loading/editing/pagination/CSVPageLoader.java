package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.immutables.value.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

@Value.Immutable
public abstract class CSVPageLoader implements PageLoader<Reader> {

    // WARNING.  ALL INSTANCES OF THESE CHARACTERS WILL BE REMOVED FROM THE CSV INPUT.
    // \uFEFF is Zero width non-breaking space often added by Windows.
    private final static String[] charactersToRemove = new String[]{"\uFEFF"};

    public interface CSVRowAccessor {
        String get(CSVRecord row);

        static CSVRowAccessor of(String column) {
            Preconditions.checkNotNull(column, "column cannot be null");
            return row -> {
                if(!row.isMapped(column)) {
                    throw new UserInputPaginationException(String.format("CSV row has no column named '%s'", column));
                }
                return row.get(column);
            };
        }
    }

    @Value.Default
    CSVRowAccessor labelAccessor() {
        return CSVRowAccessor.of("label");
    }
    @Value.Default
    CSVRowAccessor imageAccessor() {
        return CSVRowAccessor.of("image");
    }
    @Value.Default
    CSVFormat csvFormat() {
        return CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true).setIgnoreSurroundingSpaces(true)
            .setIgnoreHeaderCase(true).build();
    }

    @Override
    public List<Page> loadPages(Reader source) throws IOException {
        Preconditions.checkNotNull(source, "source cannot be null");

        // Without this CSV files created on Windows fail (they have extra empty space chars)
        source = normaliseAllLines(source);

        var pages = loadPages(Streams.stream(csvFormat().parse(source)));
        if(pages.isEmpty()) {
            throw new UserInputPaginationException("CSV contains no page data");
        }
        return pages;
    }

    /**
     * Note: This removes any of the characters in the array "charactersToRemove" from the input.
     * e.g. %uFEFF (Zero width non-breaking space) which Windows likes to add at the start of the CSV
     * files it creates.
     *
     * @param source
     * @return
     * @throws IOException
     */
    private Reader normaliseAllLines(Reader source) throws IOException {
        // Trim start and end whitespace and remove specific chars.
        BufferedReader reader = new BufferedReader(source);
        StringBuilder output = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {

            String lineNormalised = line.strip();
            for (String charToRm: charactersToRemove) {
                lineNormalised = lineNormalised.replaceAll(charToRm, "");
            }
            output.append(lineNormalised).append("\n");
            line = reader.readLine();
        }
        return new StringReader(output.toString());
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
            throw new UserInputPaginationException(String.format("Image location is not a valid URL: '%s'", image));
        }
        return ImmutablePage.builder().label(label).image(imageURI).build();
    }
}
