package funky.pom16.funkyreservation.backend;

import java.util.LinkedList;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.data.Restaurant;
import funky.pom16.funkyreservation.backend.data.RestaurantDetails;


/**
 * Created by Sebastian Reichl on 08.06.2016.
 */
public class FilterUtilityTest {
    FilterUtility util;
    Restaurant newRestaurant, newRestaurant2;
    Vector<Double> coordinates = new Vector<>(2);
    Vector<Double> coordinates2 = new Vector<>(2);
    RestaurantDetails details;
    LinkedList<Restaurant> restaurants = new LinkedList<>();

    public FilterUtilityTest(){
        this.util = new FilterUtility(null);
         /* First restaurant*/
        newRestaurant = new Restaurant("Italian Pizza");
        newRestaurant.setType("italian");
        newRestaurant.setPriceCat(5);
        newRestaurant.setRating(5);
        coordinates.add(49.123456);
        coordinates.add(18.123456);
        details = new RestaurantDetails();
        details.setUrl("https://pizza.it");
        newRestaurant.setDetails(details);
        newRestaurant.setLocation(new Vector<>(coordinates));

        /* Second restaurant */
        newRestaurant2 = new Restaurant("Indian food");
        newRestaurant2.setType("indian");
        newRestaurant2.setPriceCat(3);
        newRestaurant2.setRating(3);
        coordinates2.add(49.2234567);
        coordinates2.add(18.2234567);
        details = new RestaurantDetails();
        details.setUrl("https://indian.food");
        newRestaurant2.setDetails(details);
        newRestaurant2.setLocation(new Vector<>(coordinates2));
    }
}
