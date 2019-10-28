package uk.cam.lib.cdl.loading.apis;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class PackagingSchema {

    private final Map<String, File> schemaFiles;

    public PackagingSchema(URL url, File localPath) {

        // Get the schema file from URL if the localPath does not exist already.
        // TODO check files needed are there.
        if (!localPath.exists()) {

            this.schemaFiles = unGzipTar(url, localPath);
        } else {

            // Read in the list of files.
            Map<String, File> schemaFiles = new HashMap<>();
            try (Stream<Path> walk = Files.walk(Paths.get(localPath.getCanonicalPath()))) {

                List<File> files = walk.map(Path::toFile)
                        .filter(f -> {
                            try {
                                return f.getCanonicalPath().matches(".*/package/schemas/.*\\.json");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }).collect(Collectors.toList());

                if (files.isEmpty()) {
                    throw new FileNotFoundException("No schema files found under: " + localPath);
                }

                Map<String, File> map = new HashMap<>();
                for (File f : files) {
                    map.put(f.getName(), f);
                }
                schemaFiles = map;

            } catch (IOException e) {
                e.printStackTrace();
            }

            this.schemaFiles = schemaFiles;
        }

    }

    // copy packaged tgz from url to localPath and extract
    private Map<String, File> unGzipTar(URL url, File localPath) {

        Map<String, File> schemaFiles = new HashMap<>();

        // Save tar file from URL
        File tarFile = new File(localPath, "schema.tar");
        try (InputStream is = new GZIPInputStream(url.openStream())) {
            FileUtils.copyInputStreamToFile(is, tarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Extract files.
        try (TarArchiveInputStream tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
                .createArchiveInputStream("tar", new FileInputStream(tarFile))) {

            TarArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
                File outputFile = new File(localPath, entry.getName());
                File parentFile = outputFile.getParentFile();
                if (parentFile.exists() || parentFile.mkdirs()) {
                    OutputStream outputFileStream = new FileOutputStream(outputFile);
                    IOUtils.copy(tarInputStream, outputFileStream);
                    outputFileStream.close();
                    if (outputFile.getCanonicalPath().matches(".*/schemas/*.json")) {
                        schemaFiles.put(outputFile.getName(), outputFile);
                    }
                } else {
                    throw new IOException("Problem making dir: " + outputFile.getParent());
                }
            }

        } catch (IOException | ArchiveException e) {
            System.err.println("Problem extracting schema from tar");
            e.printStackTrace();
        }
        return schemaFiles;
    }


}
