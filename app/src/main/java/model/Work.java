package model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Work {
    private String id;
    private String title;
    private String job;
    private String salary;
    private String publisher;
    private String location;
    private Timestamp timestamp;

    public Work(String id, String title, String job, String salary, String publisher, String location,Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.job = job;
        this.salary = salary;
        this.publisher = publisher;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getJob() {
        return job;
    }

    public String getSalary() {
        return salary;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        if (timestamp == null){
            return null;
        }
        Date date = timestamp.toDate();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
