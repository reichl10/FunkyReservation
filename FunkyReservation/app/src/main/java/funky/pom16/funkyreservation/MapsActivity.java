package funky.pom16.funkyreservation;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.FilterUtility;
import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.connection.AddressResolver;
import funky.pom16.funkyreservation.backend.connection.BackendMock;
import funky.pom16.funkyreservation.backend.connection.FilterType;
import funky.pom16.funkyreservation.backend.data.Restaurant;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, OnInfoWindowClickListener {

    IBackendService backend;
    GoogleMap mMap;
    LinkedList<Restaurant> allRests;
    LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    DrawerLayout mDrawerLayout;
    //filtering drawer
    Spinner food;
    SeekBar rating, price;
    TextView selectedRating, selectedPrice, _maxPrice, _maxRating;
    EditText name;

    //details drawer
    TextView detailsName, detailsAddress, detailsCategory, detailsRating;
    ImageView websiteImage;
    RelativeLayout websiteHolder;

    private SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backend = new BackendMock();

        allRests = new LinkedList<>();

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        } catch(SecurityException ex){
        }

        sv = (SearchView) findViewById(R.id.mapSearchView);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s) {
                clearFilters();
                HashMap<FilterType, Object> filter = new HashMap<>();
                filter.put(FilterType.NAME, s);
                LinkedList<Restaurant> rests = backend.searchRestaurant(filter);

                FilterUtility fU = new FilterUtility(getBaseContext());
                fU.extractData(rests);
                LinkedList<String> types = fU.getTypes();
                Vector<Integer> priceInterval = fU.getPriceInterval();
                Vector<Integer> ratingInterval = fU.getRatingInterval();

                installFilters(types, priceInterval, ratingInterval);

                HashMap<FilterType, Object> objectMap = (HashMap<FilterType, Object>)
                        getIntent().getSerializableExtra("search_filters");
                if(objectMap != null && objectMap.size() > 0) {
                    setFilters(objectMap);
                    rests = applyFilters();
                    getIntent().removeExtra("search_filters");
                }

                allRests = rests;

                if(!rests.isEmpty()) {
                    mMap.clear();
                    LatLng lastLatLng = new LatLng(0,0);
                    for (funky.pom16.funkyreservation.backend.data.Restaurant rest : rests) {
                        Vector<Double> data = rest.getLocation();
                        double rlat = Double.parseDouble(data.get(0).toString());
                        double rlng = Double.parseDouble(data.get(1).toString());
                        LatLng latlng = new LatLng(rlat,rlng);
                        lastLatLng = latlng;
                        mMap.addMarker(new MarkerOptions()
                                .position(latlng)
                                .title(rest.getName())
                                .snippet("Click to reserve " + rest.getName()));
                    }
                    CameraUpdate cameraUpdate = calcCameraBounds(rests);
                    mMap.animateCamera(cameraUpdate);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No matches found", Toast.LENGTH_SHORT);
                    toast.show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mapViewDrawer);

        food = (Spinner) findViewById(R.id.food_map_spinner);

        _maxPrice =(TextView) findViewById(R.id.max_map_price);
        price = (SeekBar) findViewById(R.id.price_list_seekBar);
        selectedPrice = (TextView) findViewById(R.id.selected_price);
        price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedPrice.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}});

        _maxRating =(TextView) findViewById(R.id.max_map_rating);
        rating = (SeekBar) findViewById(R.id.rating_seekBar);
        selectedRating = (TextView) findViewById(R.id.selected_rating);
        rating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRating.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}});

        name = (EditText) findViewById(R.id.name_map_spinner);

        detailsName = (TextView) findViewById(R.id.restaurant_details_name);
        detailsAddress = (TextView) findViewById(R.id.restaurant_details_address);
        detailsCategory = (TextView) findViewById(R.id.restaurant_details_category);
        detailsRating = (TextView) findViewById(R.id.restaurant_details_rating);
        websiteImage = (ImageView) findViewById(R.id.restaurant_details_website);
        websiteHolder = (RelativeLayout) findViewById(R.id.restaurant_details_website_holder);
    }

    private LinkedList<Restaurant> applyFilters(){
        HashMap<FilterType, Object> map = gatherFilters();
        LinkedList<Restaurant> rests = backend.searchRestaurant(map);

        return rests;
    }

    private void installFilters(LinkedList<String> types, Vector<Integer> priceInterval,
                                Vector<Integer> ratingInterval){
        food.setVisibility(View.VISIBLE);
        rating.setVisibility(View.VISIBLE);
        price.setVisibility(View.VISIBLE);

        if(types != null && types.size() > 1) {
            types.addFirst("");
            ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
            foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            food.setAdapter(foodAdapter);
        } else {
            food.setVisibility(View.INVISIBLE);
        }

        Integer maxRating = ratingInterval.get(1);
        rating.setMax(maxRating);
        _maxRating.setText(maxRating.toString());

        Integer maxPrice = priceInterval.get(1);
        price.setMax(maxPrice);
        _maxPrice.setText(maxRating.toString());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(this);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getTitle();

                //RestaurantDetails details = backend.restaurantDetails(title);
                Restaurant restaurant = getRestaurant(title);

                String name = restaurant.getName();
                String address = AddressResolver.getAddress(restaurant, getApplicationContext());
                String category = restaurant.getType();
                Integer iRating = restaurant.getRating();
                String rating = iRating.toString();
                String image = "https://s3.amazonaws.com/kinlane-productions/bw-icons/bw-no-slash.png";
                if(restaurant.getDetails() != null && restaurant.getDetails().getPictureURLs() != null
                        && restaurant.getDetails().getPictureURLs().size() > 0)
                    image = restaurant.getDetails().getPictureURLs().getFirst();

                String sWebsite = "";
                if(restaurant.getDetails() != null && restaurant.getDetails().getUrl() != null)
                    sWebsite = restaurant.getDetails().getUrl();
                final String website = sWebsite;

                detailsName.setText(name);
                detailsAddress.setText(address);
                detailsCategory.setText(category);
                detailsRating.setText(rating);

                new DownloadImageTask((ImageView) findViewById(R.id.restaurant_details_image))
                        .execute(image);

                websiteHolder.setVisibility(View.VISIBLE);
                if(!sWebsite.isEmpty()) {
                    websiteHolder.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse(website));
                            startActivity(intent);
                        }
                    });
                } else {
                    websiteHolder.setVisibility(View.INVISIBLE);
                }

                marker.showInfoWindow();
                return true;
            }
        });

        try{
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException ex){

        }
        finally {
            SearchView sv = (SearchView) findViewById(R.id.mapSearchView);
            CharSequence seq = getIntent().getCharSequenceExtra("search_field");
            sv.setQuery(seq, seq != null);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    private Restaurant getRestaurant(String name){
        for(Restaurant r : allRests){
            if(r.getName().equals(name))
                return r;
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        //LinkedList<Integer> ids = backend.searchRestaurants(location.getLatitude(),location.getLongitude(), 5);
        LinkedList<Restaurant> rests = backend.searchRestaurants(location.getLatitude(), location.getLongitude(), 5);
        if(rests != null) {
            for (Restaurant rest : rests) {
                Vector<Double> data = rest.getLocation();
                double lat = Double.parseDouble(data.get(0).toString());
                double lng = Double.parseDouble(data.get(1).toString());
                LatLng latlng = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(latlng).title(rest.getName()));
            }
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
        mMap.animateCamera(cameraUpdate);
        try{
            locationManager.removeUpdates(this);
        } catch(SecurityException ex){

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void switchToListView(View view){
        Intent intent = new Intent(this, RestaurantListView.class);

        SearchView sv = (SearchView) findViewById(R.id.mapSearchView);
        intent.putExtra("search_field", sv.getQuery());

        HashMap<FilterType, Object> map = gatherFilters();
        intent.putExtra("search_filters", map);

        startActivity(intent);
    }

    public void showMapFilterMenu (View view)
    {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void hideMapFilterMenu (View view)
    {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    public void hideRestaurantDetails(View view) { mDrawerLayout.closeDrawer(Gravity.RIGHT); }

    public void reserveTable(View view) {
        String title = detailsName.getText().toString();
        Intent intent = new Intent(this, TableView.class);
        intent.putExtra("restaurant_id", title);
        startActivity(intent);
    }

    public void filterMapResults(View view){

        HashMap<FilterType, Object> map = gatherFilters();

        LinkedList<Restaurant> rests = backend.searchRestaurant(map);

        allRests = rests;

        if(rests != null) {
            mMap.clear();
            LatLng lastLatLng = new LatLng(0, 0);
            for (funky.pom16.funkyreservation.backend.data.Restaurant rest : rests) {
                Vector<Double> data = rest.getLocation();
                double rlat = Double.parseDouble(data.get(0).toString());
                double rlng = Double.parseDouble(data.get(1).toString());
                LatLng latlng = new LatLng(rlat, rlng);
                lastLatLng = latlng;
                mMap.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title(rest.getName())
                        .snippet("Click to reserve " + rest.getName()));
            }
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLatLng, 9);
            mMap.animateCamera(cameraUpdate);
        }
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    private HashMap<FilterType, Object> gatherFilters(){
        String sFood = "";
        if(food != null && food.getSelectedItem() != null)
            sFood = food.getSelectedItem().toString();

        int iPrice = -1;
        if(price != null)
            iPrice = price.getProgress();

        int iRating = -1;
        if(rating != null) {
            iRating = rating.getProgress();
        }

        String sName = "";
        if(name != null && name.getText() != null)
            sName = name.getText().toString();

        HashMap<FilterType, Object> map = new HashMap<>();
        if(!sFood.isEmpty()){
            map.put(FilterType.TYPE, sFood);
        }
        if(iPrice != -1 && iPrice != 0){
            map.put(FilterType.PRICE, iPrice);
        }
        if(iRating != -1 && iRating != 0){
            map.put(FilterType.RATING, iRating);
        }
        if(!sName.isEmpty()){
            map.put(FilterType.NAME, sName);
        }
        return map;
    }

    void setFilters(HashMap<FilterType, Object> map){
        if(map == null)
            return;

        for(Map.Entry<FilterType, Object> entry : map.entrySet()){
            switch(entry.getKey()){
                case NAME:
                    name.setText(entry.getValue().toString());
                    break;
                case TYPE:
                    SpinnerAdapter adapter = food.getAdapter();
                    int count = adapter.getCount();
                    for(int i = 0 ; i < count; i++){
                        String current = (String) adapter.getItem(i);
                        if(current.equals(entry.getValue())){
                            food.setSelection(i);
                            break;
                        }
                    }
                    break;
                case RATING:
                    rating.setProgress(Integer.parseInt(entry.getValue().toString()));
                    break;
                case PRICE:
                    price.setProgress(Integer.parseInt(entry.getValue().toString()));
                    break;
                default:
                    break;
            }
        }
    }

    public void onHomeClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    void clearFilters() {
        price.setProgress(0);
        rating.setProgress(0);
        food.setSelection(0);
        name.setText(null);
    }

    public void onSearchViewClick(View v) {
        sv.setIconified(false);
    }

    // When back button on the android device is pressed, the app will go back to the main menu.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent inMain = new Intent(this, MainActivity.class);
        inMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(inMain);
    }

    /**
     *
     * @param restaurants
     * @return
     */
    private CameraUpdate calcCameraBounds(LinkedList<Restaurant> restaurants) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(restaurants.size() > 1) {
            for (Restaurant res : restaurants) {
                LatLng pos = new LatLng(res.getLocation().get(0), res.getLocation().get(1));
                builder.include(pos);
            }

            LatLngBounds bounds = builder.build();
            int padding = 50; // offset from edges of the map in pixels
            return CameraUpdateFactory.newLatLngBounds(bounds, padding);
        } else if (restaurants.size() == 1) {
            LatLng pos = new LatLng(restaurants.get(0).getLocation().get(0), restaurants.get(0).getLocation().get(1));
            return CameraUpdateFactory.newLatLngZoom(pos, 15);
        } else {
            return null;
        }
    }
}


