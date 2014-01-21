package brooklyn.location.ibm.smartcloud.orchestrator.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Andrea Turli
 */
public class VirtualSystem {
    private String id;
    private String name;
    private String owner;
    private String type;
    private String created;
    private String currentstatus;
    private String currentstatus_text;
    private String defaultcloud;
    private String description;
    private List<String> hypervisors;
    private String updated;
    private String vendor;
    @SerializedName("ipaddresses")
    private IpAddresses ipaddresses;

    public VirtualSystem(String id, String name, String owner, String type, String created, String currentstatus, String currentstatus_text, String defaultcloud, String description, List<String> hypervisors, String updated, String vendor, IpAddresses ipaddresses) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.created = created;
        this.currentstatus = currentstatus;
        this.currentstatus_text = currentstatus_text;
        this.defaultcloud = defaultcloud;
        this.description = description;
        this.hypervisors = hypervisors;
        this.updated = updated;
        this.vendor = vendor;
        this.ipaddresses = ipaddresses;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getType() {
        return type;
    }

    public String getCreated() {
        return created;
    }

    public String getCurrentstatus() {
        return currentstatus;
    }

    public String getCurrentstatus_text() {
        return currentstatus_text;
    }

    public String getDefaultcloud() {
        return defaultcloud;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getHypervisors() {
        return hypervisors;
    }

    public String getUpdated() {
        return updated;
    }

    public String getVendor() {
        return vendor;
    }

    public IpAddresses getIpaddresses() {
        return ipaddresses;
    }

    @Override
    public String toString() {
        return "Cloud{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", created='" + created + '\'' +
                ", currentstatus='" + currentstatus + '\'' +
                ", currentstatus_text='" + currentstatus_text + '\'' +
                ", defaultcloud='" + defaultcloud + '\'' +
                ", description='" + description + '\'' +
                ", hypervisors=" + hypervisors +
                ", updated='" + updated + '\'' +
                ", vendor='" + vendor + '\'' +
                '}';
    }
}
