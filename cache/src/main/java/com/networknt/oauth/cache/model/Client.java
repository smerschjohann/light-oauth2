package com.networknt.oauth.cache.model;

import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class Client implements IdentifiedDataSerializable {
  private String clientId = null;

  private String clientSecret = null;

  /**
   * client type
   */
  public enum ClientTypeEnum {
    SERVER("server"),
    
    MOBILE("mobile"),
    
    SERVICE("service"),
    
    STANDALONE("standalone"),
    
    BROWSER("browser");

    private final String value;

    ClientTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ClientTypeEnum fromValue(String text) {
      for (ClientTypeEnum b : ClientTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  private ClientTypeEnum clientType = null;

  private String clientName = null;

  private String clientDesc = null;

  private String ownerId = null;

  private String scope = null;

  private String redirectUrl = null;

  private Date createDt = null;

  private Date updateDt = null;

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
   * client type
   **/
  public Client clientType(ClientTypeEnum clientType) {
    this.clientType = clientType;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client type")
  @JsonProperty("clientType")
  public ClientTypeEnum getClientType() {
    return clientType;
  }
  public void setClientType(ClientTypeEnum clientType) {
    this.clientType = clientType;
  }

  /**
   * client name
   **/
  public Client clientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client name")
  @JsonProperty("clientName")
  public String getClientName() {
    return clientName;
  }
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  /**
   * client description
   **/
  public Client clientDesc(String clientDesc) {
    this.clientDesc = clientDesc;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client description")
  @JsonProperty("clientDesc")
  public String getClientDesc() {
    return clientDesc;
  }
  public void setClientDesc(String clientDesc) {
    this.clientDesc = clientDesc;
  }

  /**
   * client owner id
   **/
  public Client ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client owner id")
  @JsonProperty("ownerId")
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * client scope separated by space
   **/
  public Client scope(String scope) {
    this.scope = scope;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client scope separated by space")
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * redirect url
   **/
  public Client redirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
    return this;
  }


  @ApiModelProperty(example = "null", required = true, value = "redirect url")
  @JsonProperty("redirectUrl")
  public String getRedirectUrl() {
    return redirectUrl;
  }
  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  /**
   * create date time
   **/
  public Client createDt(Date createDt) {
    this.createDt = createDt;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "create date time")
  @JsonProperty("createDt")
  public Date getCreateDt() {
    return createDt;
  }
  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  /**
   * update date time
   **/
  public Client updateDt(Date updateDt) {
    this.updateDt = updateDt;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "update date time")
  @JsonProperty("updateDt")
  public Date getUpdateDt() {
    return updateDt;
  }
  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
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
        Objects.equals(clientType, client.clientType) &&
        Objects.equals(clientName, client.clientName) &&
        Objects.equals(clientDesc, client.clientDesc) &&
        Objects.equals(ownerId, client.ownerId) &&
        Objects.equals(scope, client.scope) &&
        Objects.equals(redirectUrl, client.redirectUrl) &&
        Objects.equals(createDt, client.createDt) &&
        Objects.equals(updateDt, client.updateDt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientSecret, clientType, clientName, clientDesc, ownerId, scope, redirectUrl, createDt, updateDt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Client {\n");
    
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    clientType: ").append(toIndentedString(clientType)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
    sb.append("    clientDesc: ").append(toIndentedString(clientDesc)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    redirectUrl: ").append(toIndentedString(redirectUrl)).append("\n");
    sb.append("    createDt: ").append(toIndentedString(createDt)).append("\n");
    sb.append("    updateDt: ").append(toIndentedString(updateDt)).append("\n");
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

  public Client() {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.clientId = in.readUTF();
    this.clientSecret = in.readUTF();
    this.clientType = Client.ClientTypeEnum.fromValue(in.readUTF());
    this.clientName = in.readUTF();
    this.clientDesc = in.readUTF();
    this.ownerId = in.readUTF();
    this.scope = in.readUTF();
    this.redirectUrl = in.readUTF();
    this.createDt = in.readObject();
    this.updateDt = in.readObject();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.clientId);
    out.writeUTF(this.clientSecret);
    out.writeUTF(this.clientType.toString());
    out.writeUTF(this.clientName);
    out.writeUTF(this.clientDesc);
    out.writeUTF(this.ownerId);
    out.writeUTF(this.scope);
    out.writeUTF(this.redirectUrl);
    out.writeObject(this.createDt);
    out.writeObject(this.updateDt);
  }

  @Override
  public int getFactoryId() {
    return ClientDataSerializableFactory.ID;
  }

  @Override
  public int getId() {
    return ClientDataSerializableFactory.CLIENT_TYPE;
  }


}

