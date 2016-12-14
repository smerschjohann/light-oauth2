package com.networknt.oauth.service.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.LightJavaCodegen", date = "2016-12-13T18:39:02.110-05:00")
public class Service   {
  private String serviceId = null;

  private String serviceName = null;

  private String ownerName = null;

  private String ownerEmail = null;

  private String scopes = null;

  /**
   * a unique service id
   **/
  public Service serviceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "a unique service id")
  @JsonProperty("serviceId")
  public String getServiceId() {
    return serviceId;
  }
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * service name
   **/
  public Service serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service name")
  @JsonProperty("serviceName")
  public String getServiceName() {
    return serviceName;
  }
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * service owner name
   **/
  public Service ownerName(String ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service owner name")
  @JsonProperty("ownerName")
  public String getOwnerName() {
    return ownerName;
  }
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /**
   * service owner email address
   **/
  public Service ownerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service owner email address")
  @JsonProperty("ownerEmail")
  public String getOwnerEmail() {
    return ownerEmail;
  }
  public void setOwnerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
  }

  /**
   * service scopes separated by space
   **/
  public Service scopes(String scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service scopes separated by space")
  @JsonProperty("scopes")
  public String getScopes() {
    return scopes;
  }
  public void setScopes(String scopes) {
    this.scopes = scopes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Service service = (Service) o;
    return Objects.equals(serviceId, service.serviceId) &&
        Objects.equals(serviceName, service.serviceName) &&
        Objects.equals(ownerName, service.ownerName) &&
        Objects.equals(ownerEmail, service.ownerEmail) &&
        Objects.equals(scopes, service.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceId, serviceName, ownerName, ownerEmail, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Service {\n");
    
    sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
    sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    ownerEmail: ").append(toIndentedString(ownerEmail)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

