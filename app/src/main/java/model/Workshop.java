package model;

import com.google.firebase.Timestamp;

public class Workshop {
    private String id;
    private String Cover;
    private String Title;
    private String Date;
    private String Start;
    private String Location;
    private String publisher;
    private Timestamp Timestamp;

    public Workshop(String id, String Cover, String Title, String Date, String Start, String Location,String publisher) {
        this.id = id;
        this.Cover = Cover;
        this.Title = Title;
        this.Date = Date;
        this.Start = Start;
        this.Location = Location;
        this.publisher = publisher;
    }

    public Workshop(String id, String cover, String title, String date, String start, String location, String publisher, Timestamp timestamp) {
        this.id = id;
        Cover = cover;
        Title = title;
        Date = date;
        Start = start;
        Location = location;
        this.publisher = publisher;
        Timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCover() {
        return Cover;
    }

    public void setCover(String cover) {
        Cover = cover;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getStart() {
        return Start;
    }

    public void setStart(String start) {
        Start = start;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
    public Timestamp getTimestamp(){return Timestamp;}
}
