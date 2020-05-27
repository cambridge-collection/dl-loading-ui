package uk.cam.lib.cdl.loading.testutils;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import org.springframework.lang.Nullable;
import uk.cam.lib.cdl.loading.editing.itemcreation.FileContent;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Test assertions for {@link FileContent}.
 */
public class FileContentSubject extends Subject {
    private final FileContent<?> actual;

    FileContentSubject(FailureMetadata failureMetadata,
                       @Nullable FileContent<?> subject) {
        super(failureMetadata, subject);
        this.actual = subject;
    }

    /** Assert that actual file data (as well as object properties) are equal. */
    public void isContentEqualTo(@Nullable FileContent<?> expected) {
        try {
            compareForContentEquality(expected);
        } catch (IOException e) {
            throw new UncheckedIOException("Caught IOException while comparing FileContent", e);
        }
    }
    private void compareForContentEquality(@Nullable FileContent<?> expected) throws IOException {
        if(actual == null || expected == null) {
            this.isEqualTo(expected);
            return;
        }
        check("attributes()").that(actual.attributes()).isEqualTo(expected.attributes());
        check("charset()").that(actual.charset()).isEqualTo(expected.charset());
        check("representation()").that(actual.representation()).isEqualTo(expected.representation());
        if(actual.text().isPresent() && expected.text().isPresent()) {
            check("text().get().read()")
                .that(actual.text().get().read())
                .isEqualTo(expected.text().get().read());
        } else {
            check("text()").that(actual.text()).isEqualTo(expected.text());
        }
        if(actual.bytes().isPresent() && expected.bytes().isPresent()) {
            check("bytes().get().read()")
                .that(actual.bytes().get().read())
                .isEqualTo(expected.bytes().get().read());
        }
        else {
            check("bytes()").that(actual.bytes()).isEqualTo(expected.bytes());
        }

        if(!actual.contentEquals(expected)) {
            failWithActual("expected to be equal via contentEquals()", actual);
        }
    }

    public static Subject.Factory<FileContentSubject, FileContent<?>> fileContents() {
        return FileContentSubject::new;
    }

    public static FileContentSubject assertThat(@Nullable FileContent<?> actual) {
        return Truth.assertAbout(FileContentSubject.fileContents()).that(actual);
    }
}
