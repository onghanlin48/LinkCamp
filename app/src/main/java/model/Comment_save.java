package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Comment_save {
    private final String CommentID;
    private final String Comment;
    private final String PostID;
    private final String publisher;
    private final Date timestamp;

    public Comment_save(String commentID, String comment, String postID, String publisher, Date timestamp) {
        CommentID = commentID;
        Comment = comment;
        PostID = postID;
        this.publisher = publisher;
        this.timestamp = timestamp;
    }

    public String getCommentID() {
        return CommentID;
    }

    public String getComment() {
        return Comment;
    }

    public String getPostID() {
        return PostID;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }
    public Date getTimeStamp() {
        return timestamp;
    }
}
