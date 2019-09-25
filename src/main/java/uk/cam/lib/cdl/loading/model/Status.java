package uk.cam.lib.cdl.loading.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.*;

/**
 * Status
 */
@Validated
public class Status   {
  @JsonProperty("instanceId")
  private String instanceId = null;

  @JsonProperty("currentItemsVersion")
  private String currentItemsVersion = null;

  @JsonProperty("currentCollectionsVersion")
  private String currentCollectionsVersion = null;

  public Status instanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  /**
   * Get instanceId
   * @return instanceId
  **/
  @NotNull

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Status currentItemsVersion(String currentItemsVersion) {
    this.currentItemsVersion = currentItemsVersion;
    return this;
  }

  /**
   * Get currentItemsVersion
   * @return currentItemsVersion
  **/

  public String getCurrentItemsVersion() {
    return currentItemsVersion;
  }

  public void setCurrentItemsVersion(String currentItemsVersion) {
    this.currentItemsVersion = currentItemsVersion;
  }

  public Status currentCollectionsVersion(String currentCollectionsVersion) {
    this.currentCollectionsVersion = currentCollectionsVersion;
    return this;
  }

  /**
   * Get currentCollectionsVersion
   * @return currentCollectionsVersion
  **/

  public String getCurrentCollectionsVersion() {
    return currentCollectionsVersion;
  }

  public void setCurrentCollectionsVersion(String currentCollectionsVersion) {
    this.currentCollectionsVersion = currentCollectionsVersion;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Status status = (Status) o;
    return Objects.equals(this.instanceId, status.instanceId) &&
        Objects.equals(this.currentItemsVersion, status.currentItemsVersion) &&
        Objects.equals(this.currentCollectionsVersion, status.currentCollectionsVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instanceId, currentItemsVersion, currentCollectionsVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Status {\n");

    sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
    sb.append("    currentItemsVersion: ").append(toIndentedString(currentItemsVersion)).append("\n");
    sb.append("    currentCollectionsVersion: ").append(toIndentedString(currentCollectionsVersion)).append("\n");
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
