package data;


import java.util.ArrayList;

public class Workshop {
    private static final Workshop instance = new Workshop();
    public static Workshop getInstance(){
        return instance;
    }
    private static String title = null;
    private static String Description = null;
    private static String Date;
    private static String time;
    private static String etime;
    private static String location;
    private static String imageCover;
    private static int page = 0;
    private static ArrayList<String> profile = new ArrayList<>();
    private static ArrayList<String> name = new ArrayList<>();
    private static ArrayList<String> position = new ArrayList<>();
    private static int tutor;
    private static String close;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }

    public ArrayList<String> getProfile() {
        return profile;
    }

    public void setProfile(String profile,int index) {
        if(this.profile.size() < (index + 1)){
            this.profile.add(profile);
        }else{
            this.profile.set(index,profile);
        }
    }

    public ArrayList<String> getName() {
        return name;
    }

    public void setName(String name,int index) {
        if(this.name.size() < (index + 1)){
            this.name.add(name);
        }else{
            this.name.set(index,name);
        }

    }

    public ArrayList<String> getPosition() {
        return position;
    }

    public void setPosition(String position,int index) {
        if(this.position.size() < (index + 1)){
            this.position.add(position);
        }else{
            this.position.set(index,position);
        }
    }

    public int getTutor() {
        return tutor;
    }

    public void setTutor(int tutor) {
        this.tutor = tutor;
    }

    public int getPage() {
        return page;
    }

    public static void setPage(int page) {
        Workshop.page = page;
    }

    public static String getClose() {
        return close;
    }

    public static void setClose(String close) {
        Workshop.close = close;
    }

    public void clear(){
        title = null;
        Description = null;
        Date = null;
        time =null;
        etime = null;
        location = null;
        imageCover = null;
        profile.clear();
        name.clear();
        position.clear();
        tutor = 0;
        page = 0;
        close = null;
    }
}
