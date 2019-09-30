package uk.cam.lib.cdl.loading.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body
 */
@Validated
public class SubscriptionRequest {
    @JsonProperty("event")
    private String event = null;

    @JsonProperty("callbackUrl")
    private String callbackUrl = null;

    public SubscriptionRequest event(String event) {
        this.event = event;
        return this;
    }

    /**
     * Get event
     *
     * @return event
     **/
    @NotNull

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public SubscriptionRequest callbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    /**
     * Get callbackUrl
     *
     * @return callbackUrl
     **/
    @NotNull

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionRequest subscriptionRequest = (SubscriptionRequest) o;
        return Objects.equals(this.event, subscriptionRequest.event) &&
            Objects.equals(this.callbackUrl, subscriptionRequest.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, callbackUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Body {\n");

        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
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
}
