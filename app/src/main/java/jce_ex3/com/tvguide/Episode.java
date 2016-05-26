package jce_ex3.com.tvguide;

/**
 * Created by Muhammad on 5/23/2016.
 */
public class Episode {

private String title, summary, thumbnailUrl, season_num, episode_num, air_date, air_time;

    public Episode(){

    }

    public Episode(String title, String summary, String thumbnailUrl, String season_num, String episode_num, String air_date, String air_time) {

        this.title = title;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.season_num = season_num;
        this.episode_num = episode_num;
        this.air_date = air_date;
        this.air_time = air_time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
}
