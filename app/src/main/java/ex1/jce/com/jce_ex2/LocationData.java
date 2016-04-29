package ex1.jce.com.jce_ex2;

import android.provider.BaseColumns;

/**
 * Created by Muhammad on 4/29/2016.
 */
public final class LocationData {

    private String latitude;
    private String longitude;
    private String timeStamp;


    public LocationData() {

    }
    public LocationData(String latitude, String longitude, String timeStamp) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
