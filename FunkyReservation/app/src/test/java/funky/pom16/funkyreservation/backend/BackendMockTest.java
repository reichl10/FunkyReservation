package funky.pom16.funkyreservation.backend;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Calendar;
import java.util.LinkedList;

import funky.pom16.funkyreservation.backend.connection.BackendMock;
import funky.pom16.funkyreservation.backend.connection.FilterType;
import funky.pom16.funkyreservation.backend.data.PasswordUtility;
import funky.pom16.funkyreservation.backend.data.Reservation;
import funky.pom16.funkyreservation.backend.data.Restaurant;
import funky.pom16.funkyreservation.backend.data.RestaurantDetails;
import funky.pom16.funkyreservation.backend.data.Table;


/**
 * Created by manuel on 01.06.16.
 */
public class BackendMockTest {
    private IBackendService backend;

    public BackendMockTest(){
        this.backend = new BackendMock();
    }

    @Test
    public void testRestaurantDetails(){
        RestaurantDetails details;
        details = this.backend.restaurantDetails("Italian Pizza");
        System.out.println("Restaurant details are: " + details.toString());
    }

    @Test
    public void getFilteredRestaurantType() {
        HashMap<FilterType, Object> filterMask = new HashMap<>();
        filterMask.put(FilterType.TYPE, "italian");

        LinkedList<Restaurant> results = backend.searchRestaurant(filterMask);
        Assert.assertTrue(!results.isEmpty());
        Assert.assertTrue(results.getFirst().getRating() == 5);
    }

    @Test
    public void getFilteredRestaurantName() {
        HashMap<FilterType, Object> filterMask = new HashMap<>();
        filterMask.put(FilterType.NAME, "Indian food");

        LinkedList<Restaurant> results = backend.searchRestaurant(filterMask);
        Assert.assertTrue(!results.isEmpty());
        Assert.assertTrue(results.getFirst().getName() == "Indian food");
    }

    @Test
    public void getFilteredRestaurantPrice() {
        HashMap<FilterType, Object> filterMask = new HashMap<>();
        filterMask.put(FilterType.PRICE, 3);

        LinkedList<Restaurant> results = backend.searchRestaurant(filterMask);
        Assert.assertTrue(!results.isEmpty());
        Assert.assertTrue(results.getFirst().getName().equals("Indian food"));
    }

    @Test
    public void getFilteredRestaurant() {
        HashMap<FilterType, Object> filterMask = new HashMap<>();
        filterMask.put(FilterType.LATITUDE, 48.146558);
        filterMask.put(FilterType.LONGITUDE, 11.5750363);
        filterMask.put(FilterType.RADIUS, 50.0);

        LinkedList<Restaurant> results = backend.searchRestaurant(filterMask);
        Assert.assertTrue(!results.isEmpty());
        Assert.assertTrue(results.getFirst().getName().equals("Italian Pizza"));
    }

    @Test
    public void testAvailableTables() {
        String restName = "Italian Pizza";
        LinkedList<Table> slots;
        Calendar today;
        today = Calendar.getInstance();
        today.set(Calendar.AM_PM, Calendar.PM);
        today.set(Calendar.HOUR, 1);
        today.set(Calendar.MINUTE, 30);
        today.set(Calendar.SECOND, 0);

        slots = this.backend.getTimeslots(restName, today);
        System.out.println("Table is not available while reserved: " + slots);
        Assert.assertTrue(slots.isEmpty());

        today.add(Calendar.HOUR, 1);
        slots = this.backend.getTimeslots(restName, today);
        System.out.println("Table is available while not reserved: " + slots);
        Assert.assertTrue(slots.size() > 0);
        Assert.assertTrue(slots.getFirst().getNumber() == 1);
    }

    // Fail because of incompatibility of junit and Android.Base64 -> everytime null is given
    @Test
    public void attemptLogin() {
        Assert.assertEquals("tokenForEverything", this.backend.loginUser("ArnoNyhm", "p"));
        Assert.assertEquals("tokenForEverything", this.backend.loginUser("JanItor", "fp"));
        Assert.assertEquals("Invalid credentials!", this.backend.loginUser("JanItor", "incorrect"));
        Assert.assertEquals("Invalid credentials!", this.backend.loginUser("SergejFährlich", "evenFancierPassword1"));
    }

    @Test
    public void testUserReservations() {
        LinkedList<Reservation> reservationsExpected = new LinkedList<>();
        Calendar from = Calendar.getInstance();
        Calendar until;
        Reservation slot;
        from.set(Calendar.AM_PM, Calendar.PM);
        from.set(Calendar.HOUR, 11);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        until = Calendar.getInstance();
        until.setTime(from.getTime());
        until.add(Calendar.HOUR, 1);
        slot = new Reservation(from, until, new Table(1), 2, "Italian Pizza", "JanItor");
        this.backend.reserveTable("JanItor", slot);
        reservationsExpected.add(0, slot);
        slot = new Reservation(from, until, new Table(2), 2, "Indian food", "JanItor");
        this.backend.reserveTable("JanItor", slot);
        reservationsExpected.add(0, slot);

        Assert.assertEquals(reservationsExpected, this.backend.getReservations("JanItor"));
    }

    @Test
    public void testCancelling() {
        Calendar from = Calendar.getInstance();
        Calendar until;
        Reservation slot;
        from.set(Calendar.AM_PM, Calendar.PM);
        from.set(Calendar.HOUR, 7);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        until = Calendar.getInstance();
        until.setTime(from.getTime());
        until.add(Calendar.HOUR, 1);
        slot = new Reservation(from, until, new Table(2), 2, "Italian Pizza", "ArnoNyhm");
        this.backend.reserveTable("ArnoNyhm", slot);
        int id = slot.getID();

        Assert.assertEquals(3, this.backend.getReservations("ArnoNyhm").size());

        Assert.assertEquals(false, slot.getStatus());
        this.backend.confirmReservation("ArnoNyhm", id);
        Assert.assertEquals(true, slot.getStatus());

        this.backend.cancelReservation("ArnoNyhm", id);
        Assert.assertEquals(2, this.backend.getReservations("ArnoNyhm").size());
    }
}
