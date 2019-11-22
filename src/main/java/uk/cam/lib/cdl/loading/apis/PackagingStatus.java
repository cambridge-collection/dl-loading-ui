package uk.cam.lib.cdl.loading.apis;

public class PackagingStatus {

    private final String name;
    private final String type;
    private final String resultName;
    private final String resultType;

    public PackagingStatus(String name, String type, String resultName, String resultType ) {

        this.name = name;
        this.type = type;
        this.resultName = resultName;
        this.resultType = resultType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getResultName() {
        return resultName;
    }

    public String getResultType() {
        return resultType;
    }
}
