package funky.pom16.funkyreservation.backend.data;

import android.location.Address;

import java.util.Vector;


/**
 * Created by Sebastian Reichl on 25.05.2016.
 */
public class Restaurant {
    private String name;
    private String type;
    private int priceCat;
    private int rating;
    private String address = "";
    private Vector<Double> location;
    private RestaurantDetails details;

    public Restaurant(String name){
        this.name = name;
        type = "";
        priceCat = 0;
        rating = 0;
        details = new RestaurantDetails();
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

    public int getPriceCat() {
        return priceCat;
    }

    public void setPriceCat(int priceCat) {
        this.priceCat = priceCat;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Vector<Double> getLocation() {
        return location;
    }

    public void setLocation(Vector<Double> location) {
        this.location = location;
    }

    public String getAdress() {
        return this.address;
    }

    public void setAddress(Address newAddress) {
        if (!newAddress.equals(null)) {
            //Adding 0:Street, 1:Postal Code+ City, 2:Country
            this.address = newAddress.getAddressLine(0)
                    + newAddress.getAddressLine(1) + newAddress.getAddressLine(2);
        } else {
            this.address = "";
        }
    }

    public RestaurantDetails getDetails() {
        return details;
    }

    public void setDetails(RestaurantDetails details) {
        this.details = details;
    }
}
