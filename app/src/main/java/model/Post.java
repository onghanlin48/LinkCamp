package model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Post {
    private String postId;
    private String postImage;
    private String description;
    private String publisher;
    private Timestamp timestamp;

    public Post(String postId, String postImage, String description, String publisher,Timestamp timestamp) {
        this.postId = postId;
        this.postImage = postImage;
        this.description = description;
        this.publisher = publisher;
    }
    public Post(){

    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTimestamp() {
        if (timestamp == null){
            return null;
        }
        Date date = timestamp.toDate();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
