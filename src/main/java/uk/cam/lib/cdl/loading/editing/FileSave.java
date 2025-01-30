package uk.cam.lib.cdl.loading.editing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileSave {

    private static final Logger logger = LoggerFactory.getLogger(FileSave.class);

    /**
     * Saves specified InputStream to the filename and directory path specified.
     * Does not rollback as this causes issues with events triggers in s3.
     *
     * Note: Does not validate params. This should occur before calling this
     * method.
     */
    public static synchronized boolean save(String dirPath, String filename, InputStream is) throws IOException {

        // Setup variables
        File file = new File(dirPath, filename).getCanonicalFile();
        File parentDir = new File(file.getParent()).getCanonicalFile();

        try {
            // Task 1 - Create directory (if needed).
            Files.createDirectories(parentDir.toPath());

            // Task 2 - Write file to file system.
            Files.copy(is, file.toPath(), REPLACE_EXISTING);

            // Write response out in JSON.
            return true;

        } catch (IOException | RuntimeException ex) {
            logger.error("Failed to save file {}", filename, ex);
            return false;
        }

    }

    public static synchronized boolean deleteFileIfExists(Path path) throws IOException{
        try {
            if (Files.exists(path)) {
                if (Files.isDirectory(path)){
                    try (Stream<Path> entries = Files.list(path)) {
                        if (entries.findFirst().isPresent()) return false;
                    }
                }
                Files.delete(path);
                return true;
            }
        } catch (java.nio.file.NoSuchFileException e) {
            /* ignore */
        }
        return false;
    }

}
