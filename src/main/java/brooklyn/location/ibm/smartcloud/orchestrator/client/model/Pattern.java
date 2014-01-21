package brooklyn.location.ibm.smartcloud.orchestrator.client.model;

/**
 * @author Andrea Turli
 */
public class Pattern {
    private String id;
    private String name;
    private String owner;
    private String created;
    private String currentstatus;
    private String currentstatus_text;
    private String defaultcloud;
    private String description;
    private String updated;

    public Pattern(String id, String name, String owner, String created, String currentstatus, String currentstatus_text,
                   String defaultcloud, String description, String updated) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.created = created;
        this.currentstatus = currentstatus;
        this.currentstatus_text = currentstatus_text;
        this.defaultcloud = defaultcloud;
        this.description = description;
        this.updated = updated;
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

    public String getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", created='" + created + '\'' +
                ", currentstatus='" + currentstatus + '\'' +
                ", currentstatus_text='" + currentstatus_text + '\'' +
                ", defaultcloud='" + defaultcloud + '\'' +
                ", description='" + description + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}
