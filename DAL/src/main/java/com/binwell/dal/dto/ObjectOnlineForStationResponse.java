/*
 * Anonymous API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.binwell.dal.dto;

import com.google.gson.annotations.SerializedName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ObjectOnlineForStationResponse
 */

public class ObjectOnlineForStationResponse {
  @SerializedName("serverTime")
  private OffsetDateTime serverTime = null;

  @SerializedName("routeIds")
  private List<Integer> routeIds = null;

  @SerializedName("buses")
  private List<ObjectOnlineDto> buses = null;

   /**
   * Get serverTime
   * @return serverTime
  **/

  public OffsetDateTime getServerTime() {
    return serverTime;
  }

  public ObjectOnlineForStationResponse routeIds(List<Integer> routeIds) {
    this.routeIds = routeIds;
    return this;
  }

  public ObjectOnlineForStationResponse addRouteIdsItem(Integer routeIdsItem) {
    if (this.routeIds == null) {
      this.routeIds = new ArrayList<Integer>();
    }
    this.routeIds.add(routeIdsItem);
    return this;
  }

   /**
   * Get routeIds
   * @return routeIds
  **/

  public List<Integer> getRouteIds() {
    return routeIds;
  }

  public void setRouteIds(List<Integer> routeIds) {
    this.routeIds = routeIds;
  }

  public ObjectOnlineForStationResponse buses(List<ObjectOnlineDto> buses) {
    this.buses = buses;
    return this;
  }

  public ObjectOnlineForStationResponse addBusesItem(ObjectOnlineDto busesItem) {
    if (this.buses == null) {
      this.buses = new ArrayList<ObjectOnlineDto>();
    }
    this.buses.add(busesItem);
    return this;
  }

   /**
   * Get buses
   * @return buses
  **/

  public List<ObjectOnlineDto> getBuses() {
    return buses;
  }

  public void setBuses(List<ObjectOnlineDto> buses) {
    this.buses = buses;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectOnlineForStationResponse objectOnlineForStationResponse = (ObjectOnlineForStationResponse) o;
    return Objects.equals(this.serverTime, objectOnlineForStationResponse.serverTime) &&
        Objects.equals(this.routeIds, objectOnlineForStationResponse.routeIds) &&
        Objects.equals(this.buses, objectOnlineForStationResponse.buses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverTime, routeIds, buses);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ObjectOnlineForStationResponse {\n");
    
    sb.append("    serverTime: ").append(toIndentedString(serverTime)).append("\n");
    sb.append("    routeIds: ").append(toIndentedString(routeIds)).append("\n");
    sb.append("    buses: ").append(toIndentedString(buses)).append("\n");
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
