package model;

public class Tutor {
    private String profile;
    private String name;
    private String major;

    public Tutor(String profile, String name, String major) {
        this.profile = profile;
        this.name = name;
        this.major = major;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}
