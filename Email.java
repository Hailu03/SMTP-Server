import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email {
    private String from;
    private String to;
    private String subject;
    private String date;
    private String message;
    private List<Attachment> attachments;

    public Email() {
        this.attachments = new ArrayList<>();
    }

    // Getters and Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}