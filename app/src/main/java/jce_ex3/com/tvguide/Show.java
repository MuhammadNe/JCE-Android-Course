package jce_ex3.com.tvguide;

import java.util.ArrayList;

public class Show {
    private String name, thumbnailUrl, summary, showID, season_num, episode_num, air_date, air_time;


    public Show() {
    }

    public Show(String name, String thumbnailUrl, String summary, String showID, String season_num, String episode_num, String air_date, String air_time) {
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.summary = summary;
        this.showID = showID;

        this.season_num = season_num;
        this.episode_num = episode_num;
        this.air_date = air_date;
        this.air_time = air_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getShowID() {
        return showID;
    }

    public void setShowID(String showID) {
        this.showID = showID;
    }

    public String getSeason_num() {
        return season_num;
    }

    public void setSeason_num(String season_num) {
        this.season_num = season_num;
    }

    public String getEpisode_num() {
        return episode_num;
    }

    public void setEpisode_num(String episode_num) {
        this.episode_num = episode_num;
    }

    public String getAir_date() {
        return air_date;
    }

    public void setAir_date(String air_date) {
        this.air_date = air_date;
    }

    public String getAir_time() {
        return air_time;
    }

    public void setAir_time(String air_time) {
        this.air_time = air_time;
    }

    public String getAllInfo() {
        return "Season " + season_num + " ,Episode " + episode_num + " ,Air time " + air_date + " " + air_time;
    }
}