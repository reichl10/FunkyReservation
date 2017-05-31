package funky.pom16.funkyreservation.backend.connection;

import android.util.Log;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.data.User;
import funky.pom16.funkyreservation.backend.data.PasswordUtility;
import funky.pom16.funkyreservation.backend.data.Reservation;
import funky.pom16.funkyreservation.backend.data.Restaurant;
import funky.pom16.funkyreservation.backend.data.RestaurantDetails;
import funky.pom16.funkyreservation.backend.data.Table;
import funky.pom16.funkyreservation.backend.data.TimeSlot;


/**
 * @author manuel
 * @since 25.05.2016
 * <br>
 * Small backend implementation
 * <p/>
 * This class provides a very fundamental backend implementation.
 * It provides two searchable restaurants.
 * <p/>
 * Restaurants (Class): [uID, name, address, price, rating, location(lat,lon), tables] <br>
 * First restaurant: [0, Italian Pizza, 32nd street, 5, 5, (49.123456, 18.123456), []] <br>
 * Second restaurant: [1, Indian Food, 12th street, 3, 3, (49.1234567, 18.1234567), []]
 */
public class BackendMock implements IBackendService {
    MockDataStore dataStore;

    public BackendMock(){
        this.dataStore = MockDataStore.getInstance();
    }

    @Override
    public LinkedList<Restaurant> searchRestaurants(double latitude, double longitude, double radius) {
        LinkedList<Restaurant> results = new LinkedList<>();
        Vector<Double> searchVector = new Vector<>(2);
        searchVector.add(latitude);
        searchVector.add(longitude);
        boolean distanceCheck;
        for (Map.Entry<String, Restaurant> entry : this.dataStore.getRestaurantsMap().entrySet()){
            distanceCheck = this.checkDistance(entry.getValue().getLocation(), searchVector, radius);
            if (distanceCheck) {
                Restaurant res = entry.getValue();
                results.add(res);
            }
        }
        return results;
    }

    @Override
    public LinkedList<Restaurant> searchRestaurant(HashMap<FilterType, Object> filter) {
        LinkedList<Restaurant> results = new LinkedList<>();
        boolean matchedAll;

        for (Restaurant r : this.dataStore.getRestaurantsMap().values()) {
            matchedAll = true;
            if (filter.containsKey(FilterType.TYPE)
                    &&(!r.getType().equals(filter.get(FilterType.TYPE).toString()) || !matchedAll)) {
                matchedAll = false;
            }

            if (filter.containsKey(FilterType.NAME)
                    &&(!r.getName().toLowerCase().contains(filter.get(FilterType.NAME).toString().toLowerCase()) || !matchedAll)) {
                matchedAll = false;
            }

            if (filter.containsKey(FilterType.PRICE)
                    && (r.getPriceCat() != Integer.parseInt(filter.get(FilterType.PRICE).toString()) || !matchedAll)){
                matchedAll = false;
            }

            if (filter.containsKey(FilterType.RATING)
                    && (r.getRating() != Integer.parseInt(filter.get(FilterType.RATING).toString()) || !matchedAll)) {
                matchedAll = false;
            }
            //if (filter.containsKey(FilterType.START_TIME) && r.().equals(filter.get(FilterType.TYPE))
            //        && matchedAll) {
            //} else matchedAll = false;

            Vector<Double> position = new Vector<>(2);
            if (filter.containsKey(FilterType.LONGITUDE) && filter.containsKey(FilterType.LATITUDE)
                    && filter.containsKey(FilterType.RADIUS)) {
                position.add(Double.parseDouble(filter.get(FilterType.LATITUDE).toString()));
                position.add(Double.parseDouble(filter.get(FilterType.LONGITUDE).toString()));
                double radius = Double.parseDouble(filter.get(FilterType.RADIUS).toString());
                if (!checkDistance(r.getLocation(), position, radius)) matchedAll = false;
            }
            if (matchedAll) {
                results.add(r);
            }
        }
        return results;
    }

    @Override
    public RestaurantDetails restaurantDetails(String restaurantName) {
        Restaurant rest = this.dataStore.getRestaurantsMap().get(restaurantName);
        if (rest != null){
            LinkedList<TimeSlot> slots = rest.getDetails().getAvailableSlots();
            LinkedList<TimeSlot> newSlots = this.invertReservations(restaurantName);
            slots.clear();
            slots.addAll(newSlots);
            return rest.getDetails();
        }
        else return null;
    }

    /** This function inverts the list of reservations for a certain restaurant to provide available time-slots
     *
     * @param restaurantName The restaurant for which free time slot details should be generated
     * @return The available time slots for the restaurant
     */
    private LinkedList<TimeSlot> invertReservations(String restaurantName){
        // initializing variables
        LinkedList<TimeSlot> result = new LinkedList<>();

        // TODO maybe you want to add default tables, when no reservations are there
        if (this.dataStore.getReservationsMap().get(restaurantName) == null) return result;

        HashMap<Table, LinkedList<Reservation>> reservationsMap = new HashMap<>();
        Calendar lastFree, lastDate; // lastFree is the last free date, lastDate is the maximum date until which availability is checked
        lastDate = Calendar.getInstance();
        lastDate.set(Calendar.AM_PM, Calendar.PM);
        lastDate.set(Calendar.DAY_OF_MONTH, lastDate.get(Calendar.DAY_OF_MONTH) + 14);
        lastDate.set(Calendar.HOUR, 0);
        lastDate.set(Calendar.MINUTE, 0);
        lastDate.set(Calendar.SECOND, 0);
        // generating a hash map with all tables and their reservations
        for (Reservation entry : this.dataStore.getReservationsMap().get(restaurantName)){
            if (!reservationsMap.containsKey(entry.getTable())) reservationsMap.put(entry.getTable(), new LinkedList<Reservation>());
            reservationsMap.get(entry.getTable()).add(entry);
        }
        // iterating all tables
        for (HashMap.Entry<Table, LinkedList<Reservation>> entry : reservationsMap.entrySet()){
            Collections.sort(entry.getValue());
            lastFree = Calendar.getInstance();
            lastFree.set(Calendar.AM_PM, Calendar.AM);
            lastFree.set(Calendar.HOUR, 0);
            lastFree.set(Calendar.MINUTE, 0);
            lastFree.set(Calendar.SECOND, 0);
            // for each table: iterate all reservations
            for (Reservation reservation : entry.getValue()){
                // Check whether lastFree is before the first next reservation
                if (lastFree.before(reservation.getStartTime())){
                    // if yes the time between lastFree and start time is available for reservations: add it to list
                    result.add(new TimeSlot(lastFree, reservation.getStartTime(), entry.getKey(), entry.getKey().getPersons()));
                }
                // in each case: the last reservations end time is the next lastFree-time
                lastFree = reservation.getEndTime();
            }
            // after all reservations are checked: add from last end to end of time scope
            if (lastFree.before(lastDate)) {
                result.add(new TimeSlot(lastFree, lastDate, entry.getKey(), entry.getKey().getPersons()));
            }
        }
        return result;
    }

    @Override
    public LinkedList<Table> getTimeslots(String restaurantName, Calendar date) {
        LinkedList<TimeSlot> slots = this.restaurantDetails(restaurantName).getAvailableSlots();
        HashMap<Integer, Table> tables = new HashMap<>();
        Calendar start, end;
        // iterating the time slots for a restaurant
        for (TimeSlot slot : slots){
            start = slot.getStartTime();
            end = slot.getEndTime();
            // check whether the requested time is within a free time slot
            if (start.before(date) && end.after(date)){
                tables.put(slot.getTable().getNumber(), slot.getTable());
            }
        }
        return new LinkedList<>(tables.values());
    }

    @Override
    public String loginUser(String username, String pwd) {
        if (this.dataStore.getUsersMap().containsKey(username)
                && PasswordUtility.hashPassword(pwd).equals(this.dataStore.getUsersMap().get(username).getPwdHash())) {
            return "tokenForEverything";
        } else {
            return "Invalid credentials!";
        }
    }

    @Override
    public String register(String username, String pwd, String fName, String lName) {
        if (!this.dataStore.getUsersMap().containsKey(username)) {
            this.dataStore.putUser(new User(username, PasswordUtility.hashPassword(pwd), fName, lName));
            return "tokenForEverything";
        } else {
            return "Already registered";
        }
    }

    @Override
    public String register(String username, String pwd, String fName, String lName, Object picture) {
        String answer = register(username, pwd, fName, lName);
        if (answer.equals("tokenForEverything")) {
            this.dataStore.getUsersMap().get(username).setPicture(picture);
        }
        return answer;
    }

    @Override
    public LinkedList<Reservation> getReservations(String username) {
        LinkedList<Reservation> userReservations = new LinkedList<>();
        for (LinkedList<Reservation> list : this.dataStore.getReservationsMap().values()) {
            for (Reservation r : list) {
                if (r.getUserName().equals(username)) {
                    userReservations.add(r);
                }
            }
        }
        return userReservations;
    }

    @Override
    public boolean reserveTable(String username, Reservation reservation) {
        LinkedList<TimeSlot> slots = this.invertReservations(reservation.getRestaurantName());
        for (TimeSlot slot : slots) {
            if(slot.getStartTime().before(reservation.getStartTime()) &&
                    slot.getEndTime().after(reservation.getEndTime()) &&
                    slot.getTable().getNumber() == reservation.getTable().getNumber()){
                this.dataStore.addReservation(reservation.getRestaurantName(), reservation);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean confirmReservation(String username, int reservationID) {
        Reservation toConfirm = findReservationInUserReservation(username, reservationID);
        if (toConfirm != null) {
            toConfirm.setConfirmed();
            return true;
        } else return false;
    }

    @Override
    public boolean cancelReservation(String username, int reservationID) {
        Reservation toDelete = findReservationInUserReservation(username, reservationID);
        if (toDelete != null) {
            this.dataStore.deleteReservation(toDelete);
            return true;
        } else return false;
    }

    /**
     * Helper function which searches through the user reservations in order to identify
     * whether there exists this reservation for this user.
     *
     * @param username The name of the {@link User} whose {@link Reservation} is looked for
     * @param reservationID The ID of the {@link Reservation} that is looked for
     * @return A {@link Reservation} object if there is such a reservation
     *          null if there isn't a {@link Reservation} that matches the criteria
     */
    private Reservation findReservationInUserReservation(String username, int reservationID) {
        LinkedList<Reservation> userReservations = getReservations(username);
        if (!userReservations.isEmpty()) {
            for (Reservation r : userReservations) {
                if (r.getID() == reservationID) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean checkDistance(Vector<Double> coordinates1, Vector<Double> coordinates2, double distance){
        /**
         * Checking the distance between two wgs84 coordinates.
         * Not scientifically correct but a rough approximation.
         */
        double threshold = distance/5 * 0.0000001; // this is an heuristic (5m is equal to 0.0000001 degree in wgs84)
        boolean firstOkay = Math.abs(coordinates1.elementAt(0)-coordinates2.elementAt(0))<threshold;
        boolean secondOkay = Math.abs(coordinates1.elementAt(1)-coordinates2.elementAt(1))<threshold;
        return (firstOkay && secondOkay);
    }
}
