package brooklyn.location.ibm.smartcloud.orchestrator.client.features;

import brooklyn.location.ibm.smartcloud.orchestrator.client.model.DeployRequest;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.VirtualSystem;
import org.jclouds.io.payloads.StringPayload;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

import java.util.List;

/**
 * @author Andrea Turli
 */
public interface VirtualSystemInstancesAPI {

    @GET("/resources/virtualSystems")
    List<VirtualSystem> virtualSystems();

    @POST("/resources/virtualSystems")
    VirtualSystem deploy(@Body DeployRequest request);

    @GET("/resources/virtualSystems/{id}")
    VirtualSystem virtualSystem(@Path("id") String virtualSystemId);

    @DELETE("/resources/virtualSystems/{id}")
    void delete(@Path("id") String serverId);
}