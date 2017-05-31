package funky.pom16.funkyreservation.backend.connection;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.data.Restaurant;

/**
 * Created by Sebastian Reichl on 13.07.2016.
 */
public class AddressResolver {

    /**
     * Utility function to receive the location of a restaurant
     * @param restaurant The restaurant whose address should be found
     * @return The address as a String
     */
    public static String getAddress(Restaurant restaurant, Context context) {
        String address = restaurant.getAdress();
        if (!address.equals("")) {
            restaurant.setAddress(AddressResolver.resolveLatLon(restaurant.getLocation(), context));
        }
        return restaurant.getAdress();
    }

    /**
     * Used to resolve a latitude/longitude pair to an actual address that can be shown to the user
     *
     * @param location The latitude/longitude pair that is to be resolved
     * @return An {@link Address} containing the street, zip code, city and country of the
     *          requested location
     */
    private static Address resolveLatLon(Vector<Double> location, Context context) {
        if (location.size() == 2) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String errorMessage;
            List<Address> addresses = new LinkedList<>();
            double lat = location.get(0);
            double lon = location.get(1);

            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = "service_not_available";
                Log.e("Not available ", errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = "invalid_lat_long_used";
                Log.e("Invalid ", errorMessage + ". " +
                        "Latitude = " + lat + ", Longitude = " + lon, illegalArgumentException);
            }

            if (addresses.size() > 0) {
                Log.e("Address: ", addresses.get(0).toString());
                return addresses.get(0);
            }
        }

        //No address found or invalid location format
        return null;
    }
}
