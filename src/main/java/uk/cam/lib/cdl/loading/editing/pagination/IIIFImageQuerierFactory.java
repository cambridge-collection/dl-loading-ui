package uk.cam.lib.cdl.loading.editing.pagination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
// This is a bit horrible.  Ideally refactor this.
public class IIIFImageQuerierFactory {

    private static IIIFImageQuerier iiifImageQuerier;

    @Autowired
    public IIIFImageQuerierFactory(IIIFImageQuerier iiifImageQuerier) {
        IIIFImageQuerierFactory.iiifImageQuerier = iiifImageQuerier;
    }

    public static synchronized IIIFImageQuerier get()  {
        return iiifImageQuerier;
    }
}
