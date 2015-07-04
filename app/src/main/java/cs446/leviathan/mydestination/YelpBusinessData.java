package cs446.leviathan.mydestination;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Aaron Kelly-Barker on 2015-07-04.
 */
public class YelpBusinessData {

    @JsonProperty("name")
    private String name;

    @JsonProperty("rating")
    private float rating; //0-8, 0 = 1 star, 8 = 5 star

    @JsonProperty("distance")
    private double distance;

    @JsonProperty("url")
    private String url;


    public YelpBusinessData(String name, short rating, double distance, String url ) {
        this.name = name;
        this.rating = rating;
        this.distance = distance;
        this.url = url;
    }

    public String getName() {
        return name;
    }


    public double getDistance() {
        return distance;
    }

    public String getUrl() {
        return url;
    }
}
