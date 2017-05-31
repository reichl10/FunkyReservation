package funky.pom16.funkyreservation.backend.data;

import java.util.Calendar;

/**
 * Created by Sebastian Reichl on 25.05.2016.
 */
public class Reservation extends TimeSlot implements Comparable<Reservation>{
    private String restaurantName;
    private String userName;
    private boolean confirmed = false;
    private int reservationID = 0;

    public Reservation(Calendar startTime, Calendar endTime, Table table, int persons, String restaurantName, String userName) {
        super(startTime, endTime, table, persons);
        this.restaurantName = restaurantName;
        this.userName = userName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setConfirmed() {
        this.confirmed = true;
    }

    public boolean getStatus() {
        return this.confirmed;
    }

    public int getID(){
        return this.reservationID;
    }

    public void setID(int newID) {
        this.reservationID = newID;
    }

    @Override
    public int compareTo(Reservation another) {
        if (this.getStartTime().before(another.getStartTime())) return -1;
        else return 1;
    }
}
