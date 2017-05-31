package funky.pom16.funkyreservation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.LinkedList;

import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.connection.BackendMock;
import funky.pom16.funkyreservation.backend.data.Reservation;
import funky.pom16.funkyreservation.backend.data.Table;

public class TableView extends AppCompatActivity {

    IBackendService backend;

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    private TextView timeDisplay;
    private int mHour, mMinute;
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;

    private String restaurantId;
    private LinkedList<Table> allTables;

    Button reserve, cancel;

    private RelativeLayout relativeLayout;

    Integer selectedTablePosition = -1;
    Integer selectedTableId = -1;

    Context context;

    ListView lv;

    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_view);

        userName = LoginActivity.prefs.getString("username", "");

        context = this;

        backend = new BackendMock();

        dateView = (TextView) findViewById(R.id.viewTablesDate);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month, day);


        timeDisplay = (TextView) findViewById(R.id.viewTablesTime);
        timeDisplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        // get the current time
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // display the current date
        updateDisplay();

        TextView title = (TextView) findViewById(R.id.viewTableTtitle);
        String restTitle = getIntent().getStringExtra("restaurant_id");
        restaurantId = restTitle;
        title.setText( restTitle + " - reservation");

        reserve = (Button) findViewById(R.id.reserveTable);
        cancel = (Button) findViewById(R.id.cancelReserveTable);
        disableReserveButtons();

        lv = (ListView) findViewById(R.id.tablesList);

        relativeLayout = (RelativeLayout) findViewById(R.id.tableViewId);
    }

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(DATE_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, myDateListener, year, month - 1, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            year = arg1;
            month = arg2 + 1;
            day = arg3;
            showDate(year, month, day);
        }
    };

    // updates the time we display in the TextView
    private void updateDisplay() {
        timeDisplay.setText(
                new StringBuilder()
                        .append(pad(mHour)).append(":")
                        .append(pad(mMinute)));
    }

    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateDisplay();
                }
            };

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public void checkAvailability(View view){
        Calendar date = getDateFromDatePicket();

        disableReserveButtons();

        allTables = new LinkedList<>();
        allTables =  backend.getTimeslots(restaurantId, date);

        if(allTables == null || allTables.size() == 0){

            lv.setAdapter(new TablesListAdapter());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!isFinishing()){
                        new AlertDialog.Builder(TableView.this)
                                .setTitle("No available tables")
                                .setMessage("Please choose a different time or date")
                                .setCancelable(false)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).create().show();
                    }
                }
            });

            return;
        }
        lv.setAdapter(new TablesListAdapter());
        lv.setOnItemClickListener(new TableItemList());

    }

    private Calendar getDateFromDatePicket(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, mHour, mMinute, 0);
        return calendar;
    }

    class TablesListAdapter extends BaseAdapter {
        public TablesListAdapter() {

        }

        public int getCount() {
            return allTables.size();
        }

        public Object getItem(int arg0) {
            return allTables.get(arg0);
        }

        public long getItemId(int position) {
            return position;//restaurants.get(position).getuID();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.single_table_layout, parent, false);
            TextView number, persons;
            number = (TextView) row.findViewById(R.id.single_table_view_id);
            persons = (TextView) row.findViewById(R.id.single_table_view_person_count);

            Integer tableNumber = allTables.get(position).getNumber();
            number.setText("Table " + tableNumber.toString());
            if (tableNumber == 1) {
                persons.setText(tableNumber.toString() + " person");
            } else {
                persons.setText(tableNumber.toString() + " persons");
            }

            return (row);
        }
    }

    public void onHomeClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class TableItemList implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Table table = allTables.get(position);
            final Integer title = table.getNumber();

            enableReserveButtons();

            if(title == selectedTableId)
                return;

            selectedTablePosition = position;
            selectedTableId = title;
        }
    }

    public void onReserveClick(View view){
        Calendar start = getDateFromDatePicket();
        Calendar end = getDateFromDatePicket();
        end.add(Calendar.HOUR, 2);

        Table table = null;
        for (Table t : allTables) {
            if (t.getNumber() == selectedTableId) {
                table = t;
            }
        }

        final Reservation reservation = new Reservation(start, end, table, 2, restaurantId, userName);
        boolean successful = false;
        if (table != null) {
            successful = backend.reserveTable(userName, reservation);
        }



        if(successful) {
            AlertDialog.Builder resAccepted = new AlertDialog.Builder(context);

            resAccepted.setTitle(R.string.reservation_accepted_title);
            resAccepted.setMessage("Your reservation for table number " + selectedTableId + " on " + day
                    + "/" + month + "/" + year + " has been accepted.\n\n" +
                    "Please confirm your reservation at least 12 hours before its date!\n");
            resAccepted.setNegativeButton(R.string.reservations_list, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(getApplicationContext(), ReservationsView.class);
                    startActivity(intent);
                }
            });
            resAccepted.setPositiveButton(R.string.back_to_search, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }
            });

            resAccepted.setNeutralButton("Add to calendar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addToCalendar(reservation);
                }
            });

            resAccepted.setIcon(android.R.drawable.ic_dialog_alert);
            //resAccepted.setCancelable()
            resAccepted.show();
        } else {
            AlertDialog.Builder resDenied = new AlertDialog.Builder(context);

            resDenied.setTitle("Reservation declined");
            resDenied.setMessage("Your reservation was declined.\n\n" +
                    "We apologize for the inconvenience.!\n");
            resDenied.setNegativeButton(R.string.reservations_list, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(getApplicationContext(), ReservationsView.class);
                    startActivity(intent);
                }
            });

            resDenied.setIcon(android.R.drawable.ic_dialog_alert);
            //resAccepted.setCancelable()
            resDenied.show();
        }


    }

    public void onCancelClick(View view){
            finish();
    }

    private void disableReserveButtons()
    {
        reserve.setEnabled(false);
        reserve.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_out));
        reserve.setTextColor(ContextCompat.getColor(context, R.color.gray_out));
    }

    private void enableReserveButtons()
    {
        reserve.setEnabled(true);
        reserve.setBackgroundColor(ContextCompat.getColor(context, R.color.button_color));
        reserve.setTextColor(ContextCompat.getColor(context, R.color.button_text_color));
    }

    private void addToCalendar(Reservation reservation){
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", reservation.getStartTime().getTimeInMillis());
        intent.putExtra("allDay", false);
        intent.putExtra("endTime", reservation.getEndTime().getTimeInMillis());
        intent.putExtra("title", "Reservation at " + reservation.getRestaurantName());
        startActivity(intent);
    }
}
