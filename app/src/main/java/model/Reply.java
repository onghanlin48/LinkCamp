package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Reply {
    private final String replyID;
    private final String commentID;
    private final String comment;
    private final String publisher;
    private final String Name;
    private final Date timestamp;

    public Reply(String replyID, String commentID, String comment, String publisher, String name, Date timestamp) {
        this.replyID = replyID;
        this.commentID = commentID;
        this.comment = comment;
        this.publisher = publisher;
        Name = name;
        this.timestamp = timestamp;
    }

    public String getReplyID() {
        return replyID;
    }

    public String getCommentID() {
        return commentID;
    }

    public String getComment() {
        return comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getName() {
        return Name;
    }

    public String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }
    public Date getTimeStamp() {
        return timestamp;
    }
}
