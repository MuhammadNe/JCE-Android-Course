package com.swampsoftware.fastfood;

/**
 * Created by Muhammad on 7/14/2016.
 */
public class RestaurantObject {

    private String name;
    private String type;
    private String lat;
    private String lng;
    private String rate;
    private String telNumber;
    private String openTime;

    public RestaurantObject() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        if(rate.equals("null")) {
            this.rate = "";
        }else {
            this.rate = rate;
        }
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }
}
