package brooklyn.location.ibm.smartcloud.orchestrator.client;

import brooklyn.location.ibm.smartcloud.orchestrator.client.features.CloudsAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.features.PatternsAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.features.VirtualSystemInstancesAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Cloud;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.DeployRequest;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Pattern;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.VirtualSystem;
import brooklyn.util.time.Duration;
import brooklyn.util.time.Time;
import com.google.common.base.Throwables;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import retrofit.RestAdapter;
import retrofit.client.ApacheClient;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Andrea Turli
 */
public class IBMWorkloadDeployerClient {

    private RestAdapter restAdapter;
    private String username;
    private String password;

    public IBMWorkloadDeployerClient(String endpoint, String username, String password) {
        checkNotNull(endpoint, "endpoint");
        this.username = checkNotNull(username, "username");
        this.password = checkNotNull(password, "password");
        HttpClient client = configureClient();
        ApacheClient apacheClient = new ApacheClient(client);
        restAdapter = new RestAdapter.Builder()
                .setServer(endpoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(apacheClient)
                .setRequestInterceptor(
                        new IBMWorkloadDeployerInterceptor())
                .build();
    }

    private HttpClient configureClient() {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
            X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            SSLSocketFactory socketFactory = new SSLSocketFactory(trustStrategy, hostnameVerifier);
            Scheme sch = new Scheme("https", 443, socketFactory);
            httpClient.getConnectionManager().getSchemeRegistry().register(sch);

            // set credentials
            Credentials credentials = new UsernamePasswordCredentials(username, password);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), credentials);

            // set cookies for subsequent request, after the first authentication
            CookieStore cookieStore = httpClient.getCookieStore();
            BasicClientCookie zsessionid = new BasicClientCookie("zsessionid", "1390324669864fb6536d8de43deade42abaf9a23bd201d7f6fd98b6c8069");
            cookieStore.addCookie(zsessionid);
            BasicClientCookie simpleToken = new BasicClientCookie("SimpleToken",
                    "ECo+4+uz6JVawdU8gTQkqjGqr/dZDOPCUewGxbRidsWDPTTSyVbcnzENJsE9YeOrzvTxbSFOshBNiyXnom6gtIJJBTfWXZ91plpUo4P4xd+FGybX+UkjW71MVakbz3/9JKITzTem3AgjKgpCwuvaSSxdWPe09Elt0bZ8zV+iaKX5RWX0BAnA7v8ORkiCoXRJDAwOHhoDkFkggvy8pytaimSKPvWtPabiDUGREpHHL/UrtnZO/PMt/kGukUbO1mm9");
            cookieStore.addCookie(simpleToken);
            httpClient.setCookieStore(cookieStore);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return httpClient;
    }

    public RestAdapter getRestAdapter() {
        return restAdapter;
    }

    public static void main(String... args) {
        if(args.length < 3) {
            throw new IllegalArgumentException("Please insert 'endpoint', 'username', 'password'");
        }
        IBMWorkloadDeployerClient client = new IBMWorkloadDeployerClient(args[0], args[1], args[2]);

        VirtualSystemInstancesAPI api = client.getRestAdapter().create
                (VirtualSystemInstancesAPI.class);

        // Fetch and print a list of the contributors to this library.
        DeployRequest request = new DeployRequest("serverName", "/resources/patterns/3", "/resources/clouds/4",
                "rootPassword",
                "userPassword");
        VirtualSystem instance = api.deploy(request);

        Time.sleep(30 * 1000L);
        api.delete(instance.getId());
    }
}