package com.swampsoftware.fastfood;

/**
 * Created by Muhammad on 7/15/2016.
 */
public class UserCurrentLocationObject {

    private static Double lat = -1.0, lng = -1.0;

    public UserCurrentLocationObject() {

    }
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
