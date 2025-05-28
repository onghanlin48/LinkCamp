package model;

public class User {
    private final String userId;
    private final String userName;
    private final String userRole;
    private final String userProfile;

    public User(String userId, String userName, String userRole, String userProfile) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserProfile() {
        return userProfile;
    }
}
