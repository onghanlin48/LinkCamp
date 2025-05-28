package data;

public class Work {
    private static final Work instance = new Work();
    public static Work getInstance(){
        return instance;
    }
    private static String title = null;
    private static String jod_title;
    private static String type;
    private static String location;
    private static String pay = null;
    private static String minimum = null;
    private static String maximum = null;
    private static String description = null;
    private static String key = null;
    private static String requirement = null;

    public void clear(){
        title=null;
        jod_title=null;
        type=null;
        location=null;
        pay=null;
        minimum=null;
        maximum=null;
        description=null;
        key=null;
        requirement=null;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        Work.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        Work.type = type;
    }

    public String getJod_title() {
        return jod_title;
    }

    public void setJod_title(String jod_title) {
        Work.jod_title = jod_title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        Work.title = title;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        Work.pay = pay;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        Work.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(String maximum) {
        Work.maximum = maximum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Work.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        Work.key = key;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        Work.requirement = requirement;
    }
}
