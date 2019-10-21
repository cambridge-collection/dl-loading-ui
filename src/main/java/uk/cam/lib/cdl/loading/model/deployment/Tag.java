package uk.cam.lib.cdl.loading.model.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Tag
 * <p>
 * Just using name, date of tag and message at the moment.
 */
@Validated
public class Tag implements Comparable<Tag> {

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("date")
    private DateTime date = null;

    @JsonProperty("message")
    private String message = null;

    public Tag(String name, DateTime date, String message) {
        this.name = name;
        this.message = message;
        this.date = date;
    }

    public Tag name(String name) {
        this.name = name;
        return this;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tag date(DateTime date) {
        this.date = date;
        return this;
    }

    @NotNull
    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        return Objects.equals(this.name, tag.name) &&
            Objects.equals(this.date, tag.date) &&
            Objects.equals(this.message, tag.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date, message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Tag {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    date: ").append(toIndentedString(date)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @Override
    public int compareTo(Tag t1) {
        return this.getDate().compareTo(t1.getDate());
    }

}

