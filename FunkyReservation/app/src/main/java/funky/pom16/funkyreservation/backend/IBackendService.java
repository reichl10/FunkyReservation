package funky.pom16.funkyreservation.backend;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import funky.pom16.funkyreservation.backend.connection.FilterType;
import funky.pom16.funkyreservation.backend.data.Reservation;
import funky.pom16.funkyreservation.backend.data.Restaurant;
import funky.pom16.funkyreservation.backend.data.RestaurantDetails;
import funky.pom16.funkyreservation.backend.data.Table;


/**
 * Created by manuel on 30.05.16.
 */
public interface IBackendService {

    /**
     * Searches for {@link Restaurant}s within a given perimeter around a certain point.
     *
     * @param latitude The latitudinal coordinate of the point
     * @param longitude The longitudinal coordinate of the point
     * @param radius The perimeter that encloses the search
     * @return A {@link LinkedList} of {@link Restaurant}s, that are within the given perimeter
     */
    LinkedList<Restaurant> searchRestaurants(double latitude, double longitude, double radius);

    /**
     * Searches for {@link Restaurant}s that match certain filters.
     *
     * @param filter A {@link HashMap} where the key is defined by a {@link FilterType} and is for
     *               identifying what filter values are defined in the Object value
     * @return A {@link LinkedList} of {@link Restaurant}s, that meet the criteria
     */
    LinkedList<Restaurant> searchRestaurant(HashMap<FilterType, Object> filter);

    /**
     * Searches for the {@link RestaurantDetails} of a {@link Restaurant} which is identified by its ID.
     * @param restaurantName The name which identifies the {@link Restaurant} that is looked for
     * @return All details of the given {@link Restaurant}
     */
    RestaurantDetails restaurantDetails(String restaurantName);

    /**
     * Used to get all free time slots where a {@link Reservation} can take place for a given date.
     *
     * @param restaurantName The name which identifies the {@link Restaurant} that is looked for
     * @param date The {@link Calendar} date whose available slots are looked for
     * @return A {@link LinkedList} of free {@link Table}s in the {@link Restaurant} for that date
     */
    LinkedList<Table> getTimeslots(String restaurantName, Calendar date);

    /**
     * Used to log a user in, so that the backend can be accessed.
     *
     * @param username The name of the {@link funky.pom16.funkyreservation.backend.data.User}
     *                 that wants to be logged in
     * @param pwd The {@link funky.pom16.funkyreservation.backend.data.User}s password
     * @return A {@link String} token that is used to identify the user for all other functions
     *          if the login process was successful, an empty {@link String} if not
     */
    String loginUser(String username, String pwd);

    /**
     * Used to register a new {@link funky.pom16.funkyreservation.backend.data.User} in the backend.
     *
     * @param username The new {@link funky.pom16.funkyreservation.backend.data.User}s username
     * @param pwd The chosen password
     * @param fName The first name of the new {@link funky.pom16.funkyreservation.backend.data.User}
     * @param lName The last name of the new {@link funky.pom16.funkyreservation.backend.data.User}
     * @return A {@link String} token that is used to identify the user for all other functions
     *          if the registration process was successful, an empty {@link String} if not
     */
    String register(String username, String pwd, String fName, String lName);


    /**
     * Used to register a new {@link funky.pom16.funkyreservation.backend.data.User} in the backend
     * with a certain picture.
     *
     * @param username The new {@link funky.pom16.funkyreservation.backend.data.User}s username
     * @param pwd The chosen password
     * @param fName The first name of the new {@link funky.pom16.funkyreservation.backend.data.User}
     * @param lName The last name of the new {@link funky.pom16.funkyreservation.backend.data.User}
     * @param picture The picture of the new {@link funky.pom16.funkyreservation.backend.data.User}
     * @return A {@link String} token that is used to identify the user for all other functions
     *          if the registration process was successful, an empty {@link String} if not
     */
    String register(String username, String pwd, String fName, String lName, Object picture);

    /**
     * Searches for all {@link Reservation}s a user has executed so far and that are in the future.
     *
     * @param username The name identifying the {@link funky.pom16.funkyreservation.backend.data.User}
     *                 whose {@link Reservation}s are requested
     * @return A {@link LinkedList} of {@link Reservation}s; may be empty if none are found
     */
    LinkedList<Reservation> getReservations(String username);

    /**
     * Used to tell the backend that a {@link funky.pom16.funkyreservation.backend.data.User} wants
     * reserve a Table.
     *
     * @param username The name identifying the {@link funky.pom16.funkyreservation.backend.data.User}
     *                 who wants to reserve.
     * @param reservation The {@link Reservation} that the user wants to execute
     * @return True if the reservation of the table was successful, False if not
     */
    boolean reserveTable(String username, Reservation reservation);

    /**
     * Used to tell the backend, that a {@link funky.pom16.funkyreservation.backend.data.User} wants
     * to confirm his {@link Reservation}.
     *
     * @param username The name identifying the {@link funky.pom16.funkyreservation.backend.data.User}
     *                 who wants to confirm a {@link Reservation}
     * @param reservationID The {@link Reservation}s identifier
     * @return True if the confirmation was successful, False if not
     */
    boolean confirmReservation(String username, int reservationID);

    /**
     * Cancels a given {@link Reservation} of a given {@link funky.pom16.funkyreservation.backend.data.User}.
     * The {@link Reservation} does not exist anymore after calling this method.
     *
     * @param username The name identifying the {@link funky.pom16.funkyreservation.backend.data.User}
     *                 who wants to confirm a {@link Reservation}
     * @param reservationID The {@link Reservation}s identifier
     * @return True if the {@link Reservation} was cancelled successfull, False if not
     */
    boolean cancelReservation(String username, int reservationID);
}
