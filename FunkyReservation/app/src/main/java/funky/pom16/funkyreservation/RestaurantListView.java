package funky.pom16.funkyreservation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import funky.pom16.funkyreservation.backend.FilterUtility;
import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.connection.AddressResolver;
import funky.pom16.funkyreservation.backend.connection.BackendMock;
import funky.pom16.funkyreservation.backend.connection.FilterType;
import funky.pom16.funkyreservation.backend.data.Restaurant;

public class RestaurantListView extends AppCompatActivity {
    ListView l1;
    IBackendService backend;
    LinkedList<Restaurant> allRests;
    Context context;
    private SearchView sv;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        allRests = new LinkedList<>();

        backend = new BackendMock();

        setContentView(R.layout.activity_restaurant_list_view);
        l1=(ListView)findViewById(R.id.list_view_list);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.listViewDrawer);

        detailsName = (TextView) findViewById(R.id.restaurant_details_name);
        detailsAddress = (TextView) findViewById(R.id.restaurant_details_address);
        detailsCategory = (TextView) findViewById(R.id.restaurant_details_category);
        detailsRating = (TextView) findViewById(R.id.restaurant_details_rating);
        websiteImage = (ImageView) findViewById(R.id.restaurant_details_website);
        websiteHolder = (RelativeLayout) findViewById(R.id.restaurant_details_website_holder);

        food = (Spinner) findViewById(R.id.food_list_spinner);

        _maxPrice =(TextView) findViewById(R.id.max_list_price);
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

        _maxRating =(TextView) findViewById(R.id.max_list_rating);
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

        name = (EditText) findViewById(R.id.name_list_spinner);

        sv = (SearchView) findViewById(R.id.listSearchView);

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
                if (!rests.isEmpty()) {
                    l1.setAdapter(new RestaurantListAdapter(rests));
                    l1.setOnItemClickListener(new ItemList());
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

        CharSequence seq = getIntent().getCharSequenceExtra("search_field");
        sv.setQuery(seq, seq != null);
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

    public void switchToMapView(View view){
        Intent intent = new Intent(this, MapsActivity.class);

        SearchView sv = (SearchView) findViewById(R.id.listSearchView);
        intent.putExtra("search_field", sv.getQuery());

        HashMap<FilterType, Object> map = gatherFilters();
        intent.putExtra("search_filters", map);

        startActivity(intent);
    }

    public void onSearchViewClick(View v) {
        sv.setIconified(false);
    }

    class RestaurantListAdapter extends BaseAdapter {
        List<Restaurant> restaurants;

        public RestaurantListAdapter(List<Restaurant> rests) {
            restaurants = rests;
        }

        public int getCount() {
            return restaurants.size();
        }

        public Object getItem(int arg0) {
            return restaurants.get(arg0);
        }

        public long getItemId(int position) {
            return position;//restaurants.get(position).getUID();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.single_restaurant_list_view, parent, false);
            TextView title, rating, notes;
            title = (TextView) row.findViewById(R.id.listName);
            //address = (TextView) row.findViewById(R.id.listAddress);
            rating = (TextView) row.findViewById(R.id.listRating);
            notes = (TextView) row.findViewById(R.id.listNotes);
            title.setText(restaurants.get(position).getName());
            Integer price = restaurants.get(position).getPriceCat();
            detailsAddress.setText(AddressResolver.getAddress(restaurants.get(position), getApplicationContext()));

            Integer irating = restaurants.get(position).getRating();
            rating.setText(irating.toString() + "/5");
            String sPrice = "";
            for (int i = 0; i < price; i++) {
                sPrice += "$";
            }
            notes.setText(sPrice);

            return (row);
        }
    }

    private class ItemList implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //RestaurantDetails details = backend.restaurantDetails(title);
            Restaurant restaurant = allRests.get(position);

            String name = restaurant.getName();

            //String address = restaurant.getLocation().get(0) + ", "+ restaurant.getLocation().get(1);
            String address = restaurant.getAdress();
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

            mDrawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void showListFilterMenu (View view)
    {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void hideListFilterMenu (View view)
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

    public void filterListResults(View view){

        HashMap<FilterType, Object> map = gatherFilters();

        LinkedList<Restaurant> rests = backend.searchRestaurant(map);

        allRests = rests;
        l1.setAdapter(new RestaurantListAdapter(rests));
        l1.setOnItemClickListener(new ItemList());

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

    void clearFilters(){
        price.setProgress(0);
        rating.setProgress(0);
        food.setSelection(0);
        name.setText(null);
    }
}
