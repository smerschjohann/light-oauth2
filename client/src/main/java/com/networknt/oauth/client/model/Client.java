package com.networknt.oauth.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.LightJavaCodegen", date = "2016-12-13T18:37:06.905-05:00")
public class Client   {
  private String clientId = null;

  private String clientSecret = null;

  private String clientName = null;

  private String ownerName = null;

  private String ownerEmail = null;

  private String scopes = null;

  /**
   * a unique client id
   **/
  public Client clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "a unique client id")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * client secret
   **/
  public Client clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client secret")
  @JsonProperty("clientSecret")
  public String getClientSecret() {
    return clientSecret;
  }
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  /**
   * client name
   **/
  public Client clientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client name")
  @JsonProperty("clientName")
  public String getClientName() {
    return clientName;
  }
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  /**
   * client owner name
   **/
  public Client ownerName(String ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client owner name")
  @JsonProperty("ownerName")
  public String getOwnerName() {
    return ownerName;
  }
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /**
   * client owner email address
   **/
  public Client ownerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client owner email address")
  @JsonProperty("ownerEmail")
  public String getOwnerEmail() {
    return ownerEmail;
  }
  public void setOwnerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
  }

  /**
   * client scopes separated by space
   **/
  public Client scopes(String scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client scopes separated by space")
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
    Client client = (Client) o;
    return Objects.equals(clientId, client.clientId) &&
        Objects.equals(clientSecret, client.clientSecret) &&
        Objects.equals(clientName, client.clientName) &&
        Objects.equals(ownerName, client.ownerName) &&
        Objects.equals(ownerEmail, client.ownerEmail) &&
        Objects.equals(scopes, client.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientSecret, clientName, ownerName, ownerEmail, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Client {\n");
    
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
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

