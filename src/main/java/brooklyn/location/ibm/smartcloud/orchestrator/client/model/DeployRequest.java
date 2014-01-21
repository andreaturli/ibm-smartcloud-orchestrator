package brooklyn.location.ibm.smartcloud.orchestrator.client.model;


import com.google.gson.annotations.SerializedName;

/**
 * @author Andrea Turli
 */
public class DeployRequest {
    private String name;
    private String pattern;
    private String cloud;
    @SerializedName("*.ConfigPWD_ROOT.password")
    private String rootPassword;
    @SerializedName("*.ConfigPWD_USER.password")
    private String userPassword;

    public DeployRequest(String name, String pattern, String cloud, String rootPassword, String userPassword) {
        this.name = name;
        this.pattern = pattern;
        this.cloud = cloud;
        this.rootPassword = rootPassword;
        this.userPassword = userPassword;
    }

    @Override
    public String toString() {
        return "DeployRequest{" +
                "name='" + name + '\'' +
                ", pattern='" + pattern + '\'' +
                ", cloud='" + cloud + '\'' +
                ", rootPassword='" + rootPassword + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }
}
