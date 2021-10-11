package uk.cam.lib.cdl.loading.apis;

import java.io.File;
import java.io.IOException;

public class MockDeployServer {

    private final File remoteDir;

    public MockDeployServer() throws IOException{

        // Create a folder in the temp folder that will act as the remote repository
        remoteDir = File.createTempFile("remote", "");
        remoteDir.delete();
        remoteDir.mkdirs();
    }

    public File getRemoteDir() {
        return remoteDir;
    }
}
