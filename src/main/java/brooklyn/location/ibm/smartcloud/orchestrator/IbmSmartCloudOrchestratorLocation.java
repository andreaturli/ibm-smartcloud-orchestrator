package brooklyn.location.ibm.smartcloud.orchestrator;

import brooklyn.location.NoMachinesAvailableException;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.location.cloud.AbstractCloudMachineProvisioningLocation;
import brooklyn.location.cloud.CloudMachineNamer;
import brooklyn.location.ibm.smartcloud.orchestrator.client.IBMWorkloadDeployerClient;
import brooklyn.location.ibm.smartcloud.orchestrator.client.features.CloudsAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.features.PatternsAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.features.VirtualSystemInstancesAPI;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Cloud;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.DeployRequest;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.Pattern;
import brooklyn.location.ibm.smartcloud.orchestrator.client.model.VirtualSystem;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.config.ConfigBag;
import brooklyn.util.exceptions.Exceptions;
import brooklyn.util.internal.Repeater;
import brooklyn.util.time.Duration;
import brooklyn.util.time.Time;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class IbmSmartCloudOrchestratorLocation extends AbstractCloudMachineProvisioningLocation implements
        IbmSmartCloudOrchestratorConfig {

    public static final Logger LOG = LoggerFactory.getLogger(IbmSmartCloudOrchestratorLocation.class);

    private static final long serialVersionUID = -828289137296787878L;
    private static final String DEFAULT_CLOUD_NAME = "RegionTwo_nova";
    private static final String DEFAULT_IMAGE_NAME = "rhel6_OS_KVM";

    private final Map<SshMachineLocation, String> serverIds = Maps.newLinkedHashMap();
    private final Map<SshMachineLocation, String> keyPairsByLocation = MutableMap.of();
    private IBMWorkloadDeployerClient ibmWorkloadDeployerClient;

    public IbmSmartCloudOrchestratorLocation() {
        super(MutableMap.of());
    }

    @Override
    public void init() {
        ibmWorkloadDeployerClient = new IBMWorkloadDeployerClient(getEndpoint(), getIdentity(), getCredential());
    }

    public String getEndpoint() {
        return getConfig(CLOUD_ENDPOINT);
    }

    public String getIdentity() {
        return getConfig(ACCESS_IDENTITY);
    }

    public String getCredential() {
        return getConfig(ACCESS_CREDENTIAL);
    }

    public String getUser() {
        return getConfig(USER);
    }

    public String getLocation() {
        return getConfig(LOCATION);
    }

    public String getImage() {
        return getConfig(IMAGE);
    }

    public String getInstanceType() {
        return getConfig(INSTANCE_TYPE_LABEL);
    }

    public SshMachineLocation obtain(Map<?, ?> flags) throws NoMachinesAvailableException {
        ConfigBag setup = ConfigBag.newInstanceExtending(getRawLocalConfigBag(), flags);
        String serverName = new CloudMachineNamer(setup)
                .lengthMaxPermittedForMachineName(31)
                .generateNewMachineUniqueName();

        Cloud cloud = findCloud(DEFAULT_CLOUD_NAME);
        Pattern image = findImage(DEFAULT_IMAGE_NAME);
        try {
            String rootPassword = "rootPassword";
            String userPassword = "userPassword";
            VirtualSystem instance = createInstance(serverName, image.getOwner(), cloud.getOwner(), rootPassword,
                    userPassword);
            String ip = instance.getIpaddresses().getIpaddress();


            LOG.info("Using server-supplied private key for " + instance.getName() + " (" + ip + "): " +
                    "rootPassword(" + rootPassword + ") userPassword(" + userPassword + ")");
            SshMachineLocation result = registerIbmSmartCloudSshMachineLocation(ip,
                    instance.getId(), rootPassword, userPassword);
            return result;
        } catch (Exception e) {
            LOG.error(String.format("Cannot obtain a new machine with serverName(%s), imageName(%s), " +
                    "cloudName(%s)", serverName, image.getName(), cloud.getName()), e);
            throw Throwables.propagate(e);
        }
    }

    private CloudsAPI getCloudsApi() {
        return ibmWorkloadDeployerClient.getRestAdapter().create(CloudsAPI.class);
    }

    private PatternsAPI getPatternsApi() {
        return ibmWorkloadDeployerClient.getRestAdapter().create(PatternsAPI.class);
    }

    private VirtualSystemInstancesAPI getVirtualSystemInstancesApi() {
        return ibmWorkloadDeployerClient.getRestAdapter().create(VirtualSystemInstancesAPI.class);
    }

    public void release(SshMachineLocation machine) {
        try {
            String serverIdMsg = String.format("Server ID for machine(%s) must not be null", machine.getDisplayName());
            String serverId = checkNotNull(serverIds.get(machine), serverIdMsg);
            getVirtualSystemInstancesApi().delete(serverId);
            /*
            waitForInstance(Instance.Status.REMOVED, serverId,
                    getConfig(IbmSmartCloudOrchestratorConfig.CLIENT_POLL_TIMEOUT_MILLIS),
                    getConfig(IbmSmartCloudOrchestratorConfig.CLIENT_POLL_PERIOD_MILLIS));
                    */
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /*
    private String storePrivateKeyInTempFile(String keyName, String keyMaterial) throws IOException {
        File privateKey = File.createTempFile(keyName, "_rsa");
        Files.write(keyMaterial, privateKey, Charsets.UTF_8);
        Runtime.getRuntime().exec("chmod 400 " + privateKey.getAbsolutePath());
        privateKey.deleteOnExit();
        return privateKey.getAbsolutePath();
    }

    private VirtualSystem createInstanceWithRetryStrategy(int retries, String serverName, String keyName,
            String dataCenterID, String imageID, String instanceTypeID) throws Exception {
        boolean foundInstance = false;
        int failures = 0;
        VirtualSystem instance;
        do {
            instance = createInstance(serverName + "_" + failures, dataCenterID, imageID,
                    instanceTypeID, keyName);
            LOG.info("Creation requested for new SCO instance: name({}), keyname({}), location({}), id({}), " +
                    "now waiting",
                    new Object[] { instance.getName(), instance.getKeyName(), 
                    client.describeLocation(instance.getLocation()).getName(), instance.getID() });
            try {
                foundInstance = waitForInstance(Instance.Status.ACTIVE, instance.getID(),
                        getConfig(IbmSmartCloudConfig.CLIENT_POLL_TIMEOUT_MILLIS),
                        getConfig(IbmSmartCloudConfig.CLIENT_POLL_PERIOD_MILLIS));
            } catch (IllegalStateException e) {
                failures++;
                client.deleteInstance(instance.getID());
                // no need to delete keypair - reuse keyName already created before
            }
        } while (!foundInstance  && failures < retries);
        if(!foundInstance) {
            throw new RuntimeException("Instance with serverId(" + instance.getID() + ") is not running");
        }
        return client.describeInstance(instance.getID());
    }
    */
    
    private VirtualSystem createInstance(String serverName, String imageOwner, String cloudOwner,
                                         String rootPassword, String userPassword) {
        try {
            return getVirtualSystemInstancesApi().deploy(new DeployRequest(serverName, imageOwner, cloudOwner,
                    rootPassword, userPassword));
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }
/*
    private boolean waitForInstance(final Instance.Status desiredStatus, final String serverId, long timeoutMillis,
            long periodMillis) throws Exception {
        return Repeater.create("Wait until the instance is ready").until(new Callable<Boolean>() {
            public Boolean call() {
                Instance instance;
                try {
                    instance = client.describeInstance(serverId);
                } catch (Exception e) {
                    LOG.warn(String.format("Cannot find instance with serverId(%s) (continuing to wait)", serverId)+": "+e);
                    return false;
                }
                Instance.Status status = instance.getStatus();
                if (Instance.Status.FAILED.equals(desiredStatus)) {
                    LOG.warn(String.format("Instance with serverId(%s) has status=failed (throwing)", serverId));
                    throw new IllegalStateException("Instance " + instance.getName() + " has status=failed");
                }
                LOG.debug("looking for IBM SCE server " + serverId + ", status: " + status.name());
                return status.equals(desiredStatus);
            }
        }).every(periodMillis, TimeUnit.MILLISECONDS).limitTimeTo(timeoutMillis, TimeUnit.MILLISECONDS).run();
    }
    */
    protected void waitForSshable(final SshMachineLocation machine, long delayMs) {
        LOG.info("Started VM {} in {}; waiting {} for it to be sshable on {}@{}", new Object[] { 
                machine.getDisplayName(), this,
                Time.makeTimeStringRounded(delayMs), machine.getUser(), machine.getAddress(), });

        boolean reachable = new Repeater().repeat().every(1, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            public Boolean call() {
                return machine.isSshable();
            }
        }).limitTimeTo(delayMs, TimeUnit.MILLISECONDS).run();

        if (!reachable) {
            throw new IllegalStateException("SSH failed for " + machine.getUser() + "@" + machine.getAddress() + " ("
                    + getRawLocalConfigBag().getDescription() + ") after waiting " + Time.makeTimeStringRounded(delayMs));
        }
    }

    protected SshMachineLocation registerIbmSmartCloudSshMachineLocation(String ipAddress,
            String serverId, String rootPassword, String userPassword) {
        SshMachineLocation machine = createIbmSmartCloudOrchestratorSshMachineLocation(ipAddress, serverId,
                rootPassword, userPassword);
        machine.setParent(this);

        waitForSshable(machine, getConfig(IbmSmartCloudOrchestratorConfig.SSH_REACHABLE_TIMEOUT_MILLIS));

        if (getConfig(IbmSmartCloudOrchestratorConfig.SSHD_SUBSYSTEM_ENABLE)) {
            LOG.debug(this + ": machine " + ipAddress + " is sshable, enabling sshd subsystem section");
            machine.execCommands("enabling sshd subsystem",
                    ImmutableList.of(
                            "sudo sed -i \"s/#Subsystem/Subsystem/\" /etc/ssh/sshd_config",
                            "sudo /etc/init.d/sshd restart",

                            // TODO remove this and use `Apply same securityGroups rules to iptables, if iptables is running on the node`
                            "sudo service iptables stop",
                            "sudo chkconfig iptables off"));
            // wait 30s for ssh to restart (overkill, but safety first; cloud is so slow it won't matter!)
            Time.sleep(30 * 1000L);
        } else {
            LOG.debug(this + ": machine " + ipAddress + " is not yet sshable");
        }
        /*
        if (getConfig(IbmSmartCloudOrchestratorConfig.INSTALL_LOCAL_AUTHORIZED_KEYS)) {
            try {
                File authKeys = new File(Urls.mergePaths(System.getProperty("user.home"), ".ssh/authorized_keys"));
                if (authKeys.exists()) {
                    String authKeysContents = Files.toString(authKeys, Charset.defaultCharset());
                    String marker = "EOF_"+Strings.makeRandomId(8);
                    machine.execCommands("updating authorized_keys",
                            ImmutableList.of("cat >> ~/.ssh/authorized_keys << "+marker+"\n" +
                                    ""+authKeysContents.trim()+"\n"+
                                    marker+"\n"));
                }
            } catch (IOException e) {
                LOG.warn("Error installing authorized_keys to "+this+": "+e);
            }
        }
        */
        
        // TODO additional security / vulnerability fixes from cloudsoft-ibm-web project (spin / sydney)
        
        // TODO remove this (ip_tables) and use `Apply same securityGroups rules to iptables, if iptables is running on the node`
        if (getConfig(IbmSmartCloudOrchestratorConfig.STOP_IPTABLES)) {
            machine.execCommands("disabling iptables",
                    ImmutableList.of(
                        "sudo service iptables stop",
                        "sudo chkconfig iptables off"));
            Time.sleep(3 * 1000L);
        }
        serverIds.put(machine, serverId);
        return machine;
    }

    protected SshMachineLocation createIbmSmartCloudOrchestratorSshMachineLocation(String ipAddress, String serverId,
                                                                       String rootPassword, String userPassword) {
        if (LOG.isDebugEnabled())
            LOG.debug("creating IbmSmartCloudSshMachineLocation representation for {}@{}", new Object[] { getUser(),
                  ipAddress, getRawLocalConfigBag().getDescription() });
        
        MutableMap<Object, Object> props = MutableMap.builder().put("serverId", serverId)
                .put("address", ipAddress).put("displayName", ipAddress).put(USER, getUser())
                .put(PASSWORD, rootPassword).build();
        if (getManagementContext()!=null)
            return getManagementContext().getLocationManager().createLocation(props, SshMachineLocation.class);
        else
            return new SshMachineLocation(props);
    }

    @Override
    public Map<String, Object> getProvisioningFlags(Collection<String> tags) {
        // TODO if we want to support provisioning flags
        if (tags.size() > 0) {
            LOG.debug("Location {}, ignoring provisioning tags {}", this, tags);
        }
        return MutableMap.<String, Object>of();
    }

    private Cloud findCloud(final String cloudName) {
        checkNotNull(cloudName, "cloud name must not be null");
        CloudsAPI cloudsAPI = getCloudsApi();
        List<Cloud> clouds = cloudsAPI.clouds();
        Optional<Cloud> result = Iterables.tryFind(clouds, new Predicate<Cloud>() {
            public boolean apply(Cloud input) {
                return input.getName().equals(cloudName);
            }
        });
        if (!result.isPresent()) {
            LOG.warn("IBM SmartCloud Orchestration unknown cloud " + cloudName);
            LOG.info("IBM SmartCloud clouds (" + clouds.size() + ") are:");
            for (Cloud c : clouds)
                LOG.info("  " + c.getName() + " " + c.getId());
            throw new NoSuchElementException("Unknown IBM SmartCloud cloud " + cloudName);
        }
        return result.get();
    }

    private Pattern findImage(final String imageName) {
        checkNotNull(imageName, "image name must not be null");
        PatternsAPI patternsAPI = getPatternsApi();
        List<Pattern> patterns;
        try {
            patterns = patternsAPI.patterns();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Optional<Pattern> result = Iterables.tryFind(patterns, new Predicate<Pattern>() {
            public boolean apply(Pattern input) {
                return input.getName().contains(imageName);
            }
        });

        if (!result.isPresent()) {
            LOG.warn("IBM SmartCloud Orchestration unknown pattern " + imageName);
            LOG.info("IBM SmartCloud Orchestration patterns (" + patterns.size() + ") are:");
            for (Pattern img : patterns)
                LOG.info("  " + img.getName() + " " + img.getOwner() + " " + img.getId());
            throw new NoSuchElementException("Unknown IBM SmartCloud Orchestration pattern " + imageName);
        }
        return result.get();
    }

    /*
    private InstanceType findInstanceType(String imageId, final String instanceType) {
        List<InstanceType> instanceTypes;
        try {
            instanceTypes = client.describeImage(imageId).getSupportedInstanceTypes();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Optional<InstanceType> result = Iterables.tryFind(instanceTypes, new Predicate<InstanceType>() {
            public boolean apply(InstanceType input) {
                return input.getLabel().contains(instanceType);
            }
        });
        if (!result.isPresent()) {
            LOG.warn("IBM SmartCloud unknown instanceType " + instanceType);
            LOG.info("IBM SmartCloud instanceTypes (" + instanceTypes.size() + ") are:");
            for (InstanceType i : instanceTypes)
                LOG.info("  " + i.getLabel() + " " + i.getDetail() + " " + i.getId());
            throw new NoSuchElementException("Unknown IBM SmartCloud instanceType " + instanceType);
        }
        return result.get();
    }
    */
}
