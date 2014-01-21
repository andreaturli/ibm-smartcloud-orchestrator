package brooklyn.location.ibm.smartcloud.orchestrator.client.model;

/**
 * @author Andrea Turli
 */
public class IpAddresses {
    private String hostname;
    private String ipaddress;

    public IpAddresses(String hostname, String ipaddress) {
        this.hostname = hostname;
        this.ipaddress = ipaddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    @Override
    public String toString() {
        return "IpAddresses{" +
                "hostname='" + hostname + '\'' +
                ", ipaddress='" + ipaddress + '\'' +
                '}';
    }
}
