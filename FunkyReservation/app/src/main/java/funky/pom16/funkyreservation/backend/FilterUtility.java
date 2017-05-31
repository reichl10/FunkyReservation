package funky.pom16.funkyreservation.backend;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.data.Restaurant;

/**
 * Created by Sebastian Reichl on 08.06.2016.
 */
public class FilterUtility {
    private LinkedList<String> types;
    private Vector<Integer> priceInterval;
    private Vector<Integer> ratingInterval;

    Context context;

    public FilterUtility(Context _context){
        context = _context;
        priceInterval = new Vector<>();
        ratingInterval = new Vector<>();

        loadFilters();
    }

    public void safeFilters(){
        //Context context =this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(FilterUtility.class.toString(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> mySet = new LinkedHashSet<>(this.types);
        editor.putStringSet("types", mySet);
        editor.putInt("priceMin", this.priceInterval.get(0));
        editor.putInt("priceMax", this.priceInterval.get(1));
        editor.putInt("ratingMin", this.ratingInterval.get(0));
        editor.putInt("ratingMax", this.ratingInterval.get(1));
        editor.apply();
    }

    public void loadFilters(){
        //Context context = this.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(FilterUtility.class.toString(), Context.MODE_PRIVATE);
        int pMin, pMax, rMin, rMax;

        this.types = new LinkedList<>(prefs.getStringSet("types", new LinkedHashSet<String>()));

        pMin = prefs.getInt("priceMin", 0);
        pMax = prefs.getInt("priceMax", 5);
        this.priceInterval.add(pMin);
        this.priceInterval.add(pMax);

        rMin = prefs.getInt("ratingMin", 0);
        rMax = prefs.getInt("ratingMax", 5);
        this.ratingInterval.add(rMin);
        this.ratingInterval.add(rMax);
    }

    /**
     * Checks for a given list of restaurants if there are any unknown found types.
     * @param restaurants The list of restaurant that is to be looked through
     */
    public void extractData(LinkedList<Restaurant> restaurants) {
        for (Restaurant actual : restaurants) {
            int price = actual.getPriceCat();
            int rating = actual.getRating();
            String type = actual.getType();
            checkPrice(price);
            checkRating(rating);
            checkType(type);

        }
    }

    /**
     * Checks if the given price is lower or higher than the current borders and sets the new
     * borders accordingly
     * @param check The price to check against the given ones
     */
    private void checkPrice(int check) {
        if (check < priceInterval.get(0)) {
            priceInterval.set(0, check);
        } else if (check > priceInterval.get(1)) {
            priceInterval.set(1, check);
        }
    }

    /**
     * Checks if the given rating is higher or lower than the current borders and sets the new
     * borders accordingly.
     * @param check
     */
    private void checkRating(int check) {
        if (check < ratingInterval.get(0)) {
            ratingInterval.set(0, check);
        } else if (check > ratingInterval.get(1)) {
            ratingInterval.set(1, check);
        }
    }

    /**
     * Checks the given type if it contained in the types list.
     * @param type The given type that is to be checked
     */
    private void checkType(String type) {
        if (!types.contains(type)) {
            addAlphabeticallySorted(type);
        }
    }

    /**
     * Adds a new type in the types list and keeps it alphabetically sorted.
     * @param type The type that is to be entered
     */
    public void addAlphabeticallySorted(String type){
        String previous = "";
        if (types.isEmpty()) {
            types.add(type);
        } else {
            String current = types.getFirst();
            int i = 0;
            while (current != null && type.compareTo(current) >= 0 && i < types.size()) {
                previous = current;
                current = types.get(i);
                i++;
            }

            if (previous == "") {
                types.add(0, type);
            } else {
                types.add(i, type);
            }
        }
    }

    public LinkedList<String> getTypes() {
        return types;
    }

    public Vector<Integer> getPriceInterval() {
        return priceInterval;
    }

    public Vector<Integer> getRatingInterval() {
        return ratingInterval;
    }
}
