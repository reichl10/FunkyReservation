package funky.pom16.funkyreservation.backend.data;

import java.util.LinkedList;

/**
 * Created by Sebastian Reichl on 30.05.2016.
 */
public class RestaurantDetails {
    // the lists can be accesed with the get and modified directly
    private LinkedList<Object> comments = new LinkedList<>();
    private LinkedList<String> pictureURLs = new LinkedList<>();
    private LinkedList<Object> openingHours = new LinkedList<>();
    private LinkedList<TimeSlot> availableSlots = new LinkedList<>();

    // the URL need getters and setters
    private String url = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LinkedList<Object> getComments() {
        return comments;
    }

    public LinkedList<String> getPictureURLs() {
        return pictureURLs;
    }

    public void setPictureURLs(LinkedList<String> _list) {
        pictureURLs = _list;
    }

    public LinkedList<Object> getOpeningHours() {
        return openingHours;
    }

    public LinkedList<TimeSlot> getAvailableSlots() {
        return availableSlots;
    }

    public String toString(){
        return String.format("Comments:%s\nPictures:%s\nTimeSlots:%s\nURL:%s",
                this.comments, this.pictureURLs, this.availableSlots, this.url);
    }
}
