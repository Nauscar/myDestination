package cs446.leviathan.mydestination.yelp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Aaron Kelly-Barker on 2015-07-04.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class YelpBusinessData {

    @JsonProperty("name")
    private String name;

    @JsonProperty("rating")
    private float rating; //1-5

    @JsonProperty("distance")
    private double distance; //In meters

    @JsonProperty("mobile_url")
    private String url;

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