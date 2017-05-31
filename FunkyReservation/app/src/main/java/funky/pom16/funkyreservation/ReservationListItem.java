package funky.pom16.funkyreservation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import funky.pom16.funkyreservation.backend.data.Reservation;

/**
 * Created by manuel on 15.06.16.
 */
public class ReservationListItem extends BaseAdapter {
    private Context context;
    private List<Reservation> reservations;

    public ReservationListItem(Context context, List<Reservation> objects) {
        super();
        this.context = context;
        this.reservations = objects;
    }

    public void updateReservations(List<Reservation> reservations){
        this.reservations = reservations;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.reservations.size();
    }

    @Override
    public Object getItem(int position) {
        return this.reservations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.reservation_item, parent, false);
        TextView restaurant_view, start_view, table_view, persons_view, confirmed;
        restaurant_view = (TextView) row.findViewById(R.id.restName);
        restaurant_view.setText(this.reservations.get(position).getRestaurantName());
        start_view = (TextView) row.findViewById(R.id.date);
        Calendar startTime = this.reservations.get(position).getStartTime();
        DateFormat fmt = SimpleDateFormat.getDateTimeInstance();
        start_view.setText(String.format("%s", fmt.format(startTime.getTime())));
        table_view = (TextView) row.findViewById(R.id.table);
        table_view.setText(String.format("Table: %s", this.reservations.get(position).getTable().getNumber()));
        persons_view = (TextView) row.findViewById(R.id.persons);
        persons_view.setText(String.format("%s Persons", this.reservations.get(position).getPersons()));
        confirmed = (TextView) row.findViewById(R.id.confirmed);
        if (this.reservations.get(position).getStatus()){
            confirmed.setText("Confirmed");
            confirmed.setTextColor(Color.GREEN);
        } else {
            confirmed.setText("Not Confirmed");
            confirmed.setTextColor(Color.RED);
        }

        return (row);

    }
}
