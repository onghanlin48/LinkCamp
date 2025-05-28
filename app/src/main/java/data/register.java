package data;

public class register {
    private static final register instance = new register();
    private String ic;
    private String name;
    private String email;

    private String password;

    private String salt;

    private String ic_front;
    private String ic_back;
    private String profile;
    private String certificates;

    private int otp;
    private String role;
    private String hashOTp;

    public String getHashOTp() {
        return hashOTp;
    }

    public void setHashOTp(String hashOTp) {
        this.hashOTp = hashOTp;
    }

    private int page;
    //1 = create acc 2 = reset password 3 = login  4 = change password
    //5 = change_verify_email
    //6 = change_verify_new_email
    //7 = chane_password
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getCertificates() {
        return certificates;
    }

    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getIc_back() {
        return ic_back;
    }

    public void setIc_back(String ic_back) {
        this.ic_back = ic_back;
    }

    public String getIc_front() {
        return ic_front;
    }

    public void setIc_front(String ic_front) {
        this.ic_front = ic_front;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public static  register getInstance(){
        return instance;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getEmail(){
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt(){
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }
    public void setRole(String role){
        this.role = role;
    }
    public String getRole(){
        return role;
    }

    public void clear(){
        ic = null;
        name = null;
        email = null;
        password = null;
        salt = null;
        role = null;
        otp = 0;
        ic_front = null;
        ic_back = null;
        profile = null;
        certificates = null;
        page = 0;
        hashOTp =null;
    }
}
