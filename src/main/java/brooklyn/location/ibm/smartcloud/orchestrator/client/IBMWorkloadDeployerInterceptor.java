package brooklyn.location.ibm.smartcloud.orchestrator.client;

import retrofit.RequestInterceptor;

/**
 * @author Andrea Turli
 */
public class IBMWorkloadDeployerInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("X-IBM-Workload-Deployer-API-Version", "3.0");
        request.addHeader("Content-Type", "application/json");
    }
}

