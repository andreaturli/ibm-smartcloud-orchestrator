package brooklyn.location.ibm.smartcloud.orchestrator.client.features;

import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Cloud;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

/**
 * @author Andrea Turli
 */
public interface CloudsAPI {

    @GET("/resources/clouds")
    List<Cloud> clouds();

    @GET("/resources/clouds/{id}")
    Cloud cloud(@Path("id") String cloudId);
}