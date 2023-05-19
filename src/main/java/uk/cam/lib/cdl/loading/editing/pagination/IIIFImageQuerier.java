package uk.cam.lib.cdl.loading.editing.pagination;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class IIIFImageQuerier {

    private final String iiifImageServer;

    public IIIFImageQuerier(String iiifImageServer) {
        if (iiifImageServer==null) {
            throw new RuntimeException("The value for the iiif server is null.");
        }
        this.iiifImageServer = iiifImageServer;
    }

    // e.g. MS-ADD-03958-001-00001.jp2 would query
    // https://images.lib.cam.ac.uk/iiif/MS-ADD-03958-001-00001.jp2/info.json
    // to get height and width info.
    public IIIFImageInfo getImageInfo(String jp2) throws IOException {

        //  query image server
        // https://images.lib.cam.ac.uk/iiif/MS-ADD-03958-001-00001.jp2/info.json
        String urlInput = iiifImageServer+jp2+".jp2/info.json";

        URL url = new URL(urlInput);
        URLConnection request = url.openConnection();
        request.connect();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(request.getInputStream(), IIIFImageInfo.class);

    }
}
