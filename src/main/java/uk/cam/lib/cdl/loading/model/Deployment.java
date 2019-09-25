package uk.cam.lib.cdl.loading.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Deployment
 */
@Validated
public class Deployment   {
  @JsonProperty("deploymentComplete")
  private Boolean deploymentComplete = null;

  @JsonProperty("instanceRequest")
  private Instance instanceRequest = null;

  @JsonProperty("instanceStatus")
  private Status instanceStatus = null;

  public Deployment deploymentComplete(Boolean deploymentComplete) {
    this.deploymentComplete = deploymentComplete;
    return this;
  }

  /**
   * Get deploymentComplete
   * @return deploymentComplete
  **/
  @NotNull

  public Boolean isDeploymentComplete() {
    return deploymentComplete;
  }

  public void setDeploymentComplete(Boolean deploymentComplete) {
    this.deploymentComplete = deploymentComplete;
  }

  public Deployment instanceRequest(Instance instanceRequest) {
    this.instanceRequest = instanceRequest;
    return this;
  }

  /**
   * Get instanceRequest
   * @return instanceRequest
  **/
  @NotNull

  @Valid
  public Instance getInstanceRequest() {
    return instanceRequest;
  }

  public void setInstanceRequest(Instance instanceRequest) {
    this.instanceRequest = instanceRequest;
  }

  public Deployment instanceStatus(Status instanceStatus) {
    this.instanceStatus = instanceStatus;
    return this;
  }

  /**
   * Get instanceStatus
   * @return instanceStatus
  **/
  @NotNull

  @Valid
  public Status getInstanceStatus() {
    return instanceStatus;
  }

  public void setInstanceStatus(Status instanceStatus) {
    this.instanceStatus = instanceStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Deployment deployment = (Deployment) o;
    return Objects.equals(this.deploymentComplete, deployment.deploymentComplete) &&
        Objects.equals(this.instanceRequest, deployment.instanceRequest) &&
        Objects.equals(this.instanceStatus, deployment.instanceStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentComplete, instanceRequest, instanceStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Deployment {\n");

    sb.append("    deploymentComplete: ").append(toIndentedString(deploymentComplete)).append("\n");
    sb.append("    instanceRequest: ").append(toIndentedString(instanceRequest)).append("\n");
    sb.append("    instanceStatus: ").append(toIndentedString(instanceStatus)).append("\n");
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
