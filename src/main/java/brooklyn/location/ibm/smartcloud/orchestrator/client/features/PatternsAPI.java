package brooklyn.location.ibm.smartcloud.orchestrator.client.features;

import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Cloud;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Pattern;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

/**
 * @author Andrea Turli
 */
public interface PatternsAPI {

    @GET("/resources/patterns")
    List<Pattern> patterns();

    @GET("/resources/patterns/{id}")
    Pattern pattern(@Path("id") String patternId);
}