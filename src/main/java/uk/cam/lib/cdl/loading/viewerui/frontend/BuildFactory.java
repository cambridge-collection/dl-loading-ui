package uk.cam.lib.cdl.loading.viewerui.frontend;

import uk.cam.lib.cdl.loading.viewerui.frontend.frontend.FrontEndBuild;

/**
 * Created by hal on 06/10/15.
 */
public interface BuildFactory {
    public FrontEndBuild getBuild(PageType pageType);

}
