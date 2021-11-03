package uk.cam.lib.cdl.loading.model.logs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity(name = "data_processing_errors")
@Table(name = "data_processing_errors")
public class LogMessage {

    @Column(nullable = false, name = "message_id")
    @Id
    private String messageId;

    @Column(nullable = false, name = "topic_arn")
    private String topic_arn;

    @Column(nullable = false, name = "timestamp")
    private LocalDateTime timestamp;

    @Column(nullable = false, name = "subject")
    private String subject;

    @Column(nullable = false, name = "message")
    private String message;

    @Column(nullable = false, name = "unsubscribe_url")
    private String unsubscribe_url;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String message_id) {
        this.messageId = message_id;
    }

    public String getTopic_arn() {
        return topic_arn;
    }

    public void setTopic_arn(String topic_arn) {
        this.topic_arn = topic_arn;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public String getUnsubscribe_url() {
        return unsubscribe_url;
    }

    public void setUnsubscribe_url(String unsubscribe_url) {
        this.unsubscribe_url = unsubscribe_url;
    }
}
