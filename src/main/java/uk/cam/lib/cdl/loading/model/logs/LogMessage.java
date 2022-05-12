package uk.cam.lib.cdl.loading.model.logs;

import org.json.JSONObject;

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
    private String topicArn;

    @Column(nullable = false, name = "timestamp")
    private LocalDateTime timestamp;

    @Column(nullable = false, name = "subject")
    private String subject;

    @Column(nullable = false, name = "message")
    private String message;

    @Column(nullable = false, name = "unsubscribe_url")
    private String unsubscribeUrl;

    @Column(nullable = false, name = "log_type")
    private LogType logType;

    @Column(nullable = true, name = "log_group")
    private String logGroup;

    @Column(nullable = true, name = "log_stream")
    private String logStream;

    @Column(nullable = true, name = "json_event")
    private String jsonEvent;

    @Column(nullable = true, name = "error")
    private String error;

    @Column(nullable = true, name = "stacktrace")
    private String stacktrace;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String message_id) {
        this.messageId = message_id;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topic_arn) {
        this.topicArn = topic_arn;
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

    public String getUnsubscribeUrl() {
        return unsubscribeUrl;
    }

    public void setUnsubscribeUrl(String unsubscribe_url) {
        this.unsubscribeUrl = unsubscribe_url;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType log_type) {
        this.logType = log_type;
    }

    public String getLogGroup() {
        return logGroup;
    }

    public void setLogGroup(String log_group) {
        this.logGroup = log_group;
    }

    public String getLogStream() {
        return logStream;
    }

    public void setLogStream(String log_stream) {
        this.logStream = log_stream;
    }

    public String getJsonEvent() {
        return jsonEvent;
    }

    public void setJsonEvent(String json_event) {
        this.jsonEvent = json_event;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getCloudwatchLink() {

        String awsregion = System.getenv("AWS_DEFAULT_REGION");

        return "https://"+awsregion+".console.aws.amazon.com/cloudwatch/home?region="+awsregion+"#logsV2:log-groups/log-group/"+
            getLogGroup().replace("/", "$252F") +"/log-events/"+ getLogStream().replace("$", "$2524")
            .replace("[", "$255B").replace("]", "$255D").replace("/", "$252F");

    }

    public String getSourceFileChanged() {
        String json = getJsonEvent();
        if (json == null) {
            return null;
        }

        JSONObject obj = new JSONObject(json);
        JSONObject record = obj.getJSONArray("Records").getJSONObject(0);
        return record.getJSONObject("s3").getJSONObject("object").getString("key");
    }

    public String getSourceFileS3Link() {
        String json = getJsonEvent();
        if (json == null) {
            return null;
        }

        JSONObject obj = new JSONObject(json);
        JSONObject record = obj.getJSONArray("Records").getJSONObject(0);
        String key = record.getJSONObject("s3").getJSONObject("object").getString("key");
        String bucket = record.getJSONObject("s3").getJSONObject("bucket").getString("name");
        String awsRegion = record.getString("awsRegion");

        return "https://"+bucket+".s3."+awsRegion+".amazonaws.com/"+key;
    }
}
