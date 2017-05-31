package funky.pom16.funkyreservation.backend.connection;

import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.data.PasswordUtility;
import funky.pom16.funkyreservation.backend.data.Reservation;
import funky.pom16.funkyreservation.backend.data.Restaurant;
import funky.pom16.funkyreservation.backend.data.RestaurantDetails;
import funky.pom16.funkyreservation.backend.data.Table;
import funky.pom16.funkyreservation.backend.data.User;

/**
 * Singleton class for managing mock data.
 * Is singleton to keep data between different backends consistent.
 *
 * @author Sebastian Reichl
 * @since 15.06.2016
 */
public class MockDataStore {

    private static MockDataStore instance = new MockDataStore();

    private HashMap<String, Restaurant> restaurants = new HashMap<>();
    private HashMap<String, LinkedList<Reservation>> reservations = new HashMap<>();
    private HashMap<String, User> users = new HashMap<>();
    private int reservationCounter = 0;

    public static MockDataStore getInstance() {
        return instance;
    }

    private MockDataStore() {
        Restaurant newRestaurant, newRestaurant2, newRestaurant3;
        Vector<Double> coordinates = new Vector<>(2);
        Vector<Double> coordinates2 = new Vector<>(2);
        Vector<Double> coordinates3 = new Vector<>(2);
        RestaurantDetails details;
        Reservation slot;
        Calendar from, until;

        /* First restaurant*/
        newRestaurant = new Restaurant("Italian Pizza");
        newRestaurant.setType("italian");
        newRestaurant.setPriceCat(5);
        newRestaurant.setRating(5);
        coordinates.add(48.146558);
        coordinates.add(11.5750363);
        details = new RestaurantDetails();
        details.setUrl("https://pizza.it");
        newRestaurant.setDetails(details);
        newRestaurant.setLocation(coordinates);

        /* Second restaurant */
        newRestaurant2 = new Restaurant("Indian food");
        newRestaurant2.setType("indian");
        newRestaurant2.setPriceCat(3);
        newRestaurant2.setRating(3);
        coordinates2.add(48.139327);
        coordinates2.add(11.5631626);
        details = new RestaurantDetails();
        details.setUrl("https://indian.food");
        newRestaurant2.setDetails(details);
        newRestaurant2.setLocation(coordinates2);

        /* Third restaurant*/
        newRestaurant3 = new Restaurant("Hofbräuhaus München");
        newRestaurant3.setType("bavarian");
        newRestaurant3.setPriceCat(6);
        newRestaurant3.setRating(8);
        coordinates3.add(48.1376098);
        coordinates3.add(11.5799253);
        details = new RestaurantDetails();
        details.setUrl("https://www.hofbraeu-muenchen.de");
        newRestaurant3.setDetails(details);
        newRestaurant3.setLocation(coordinates3);

        /* adding generated*/
        this.restaurants.put(newRestaurant.getName(), newRestaurant);
        this.restaurants.put(newRestaurant2.getName(), newRestaurant2);
        this.restaurants.put(newRestaurant3.getName(), newRestaurant3);

        // adding reservations
        from = Calendar.getInstance();
        from.set(Calendar.AM_PM, Calendar.PM);
        from.set(Calendar.HOUR, 1);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        until = Calendar.getInstance();
        until.setTime(from.getTime());
        until.add(Calendar.HOUR, 1);
        slot = new Reservation(from, until, new Table(1), 2, newRestaurant.getName(), "someUser");
        this.addReservation(newRestaurant.getName(), slot);
        slot = new Reservation(from, until, new Table(2), 2, newRestaurant.getName(), "someUser");
        this.addReservation(newRestaurant.getName(), slot);
        slot = new Reservation(from, until, new Table(1), 2, newRestaurant3.getName(), "ArnoNyhm");
        this.addReservation(newRestaurant3.getName(), slot);

        from = Calendar.getInstance();
        from.set(Calendar.AM_PM, Calendar.PM);
        from.set(Calendar.HOUR, 2);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        until = Calendar.getInstance();
        until.setTime(from.getTime());
        until.add(Calendar.HOUR, 1);
        slot = new Reservation(from, until, new Table(2), 3, newRestaurant2.getName(), "someUser");
        this.addReservation(newRestaurant2.getName(), slot);

        from.add(Calendar.HOUR, 3);
        until.add(Calendar.HOUR, 3);
        slot = new Reservation(from, until, new Table(2), 3, newRestaurant3.getName(), "ArnoNyhm");
        this.addReservation(newRestaurant3.getName(), slot);

        //Adding first user
        User arno = new User("ArnoNyhm", PasswordUtility.hashPassword("p"), "Arno", "Nyhm");
        users.put("ArnoNyhm", arno);

        //Adding second user
        User jan = new User("JanItor", PasswordUtility.hashPassword("fp"), "Jan", "Itor");
        users.put("JanItor", jan);
    }

    public HashMap<String, Restaurant> getRestaurantsMap() {
        return restaurants;
    }

    public HashMap<String, LinkedList<Reservation>> getReservationsMap() {
        return reservations;
    }

    /**
     * Helper function to add a reservation to the reservations map,
     * even if there is no reservations-list for selected restaurant
     * @param restaurantName The restaurants name, where the reservations is
     * @param reservation The reservation details
     */
    public void addReservation(String restaurantName, Reservation reservation){
        reservationCounter++;
        reservation.setID(reservationCounter);

        if (! this.reservations.containsKey(restaurantName)){
            LinkedList<Reservation> restaurantReservations = new LinkedList<>();
            this.reservations.put(restaurantName, restaurantReservations);
        }
        this.reservations.get(restaurantName).add(reservation);
    }

    /**
     * Deletes a given {@link Reservation} from the reservations map.
     * Does nothing, if reservation doesn't exist
     *
     * @param toDelete The reservation that is to be removed
     */
    public void deleteReservation(Reservation toDelete) {
        this.reservations.get(toDelete.getRestaurantName()).remove(toDelete);
    }

    public HashMap<String, User> getUsersMap() {
        return users;
    }

    /**
     * Puts a new {@link User} to the user list.
     * @param newUsers
     */
    public void putUser(User newUsers) {
        this.users.put(newUsers.getUsername(), newUsers);
    }
}
