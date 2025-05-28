package model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Learn {
    private String id;
    private String title;
    private String description;
    private String publisher;
    private Boolean channel;
    private Timestamp timestamp;

    public Learn(String id, String title, String description, String publisher,Boolean channel,Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
        this.channel =channel;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPublisher() {
        return publisher;
    }
    public Boolean getChannel() {
        return channel;
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
