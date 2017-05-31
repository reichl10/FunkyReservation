package funky.pom16.funkyreservation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Calendar;
import java.util.LinkedList;

import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.connection.BackendMock;
import funky.pom16.funkyreservation.backend.data.Reservation;

public class ReservationsView extends AppCompatActivity {
    // TODO somehow "share" backend
    private IBackendService bService;
    private LinkedList<Reservation> reservations;
    private ReservationListItem adapter;
    private int selectionPosition = -1;
    private Context context;

    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations_view);
        this.context = this;

        userName = LoginActivity.prefs.getString("username", "");

        // add action bar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        if (bar != null) {
            setSupportActionBar(bar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.bService = new BackendMock();

        this.reservations = bService.getReservations(userName);

        ListView list_view;
        list_view = (ListView) findViewById(R.id.reservations_list);
        this.adapter = new ReservationListItem(this.getBaseContext(), this.reservations);
        list_view.setAdapter(this.adapter);
        list_view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list_view.setOnItemClickListener(new ReservationClickListener(this));
    }

    private void confirmReservation(int reservationID){
        Reservation reservation = this.reservations.get(reservationID);
        this.bService.confirmReservation(userName, reservation.getID());
        this.reservations = this.bService.getReservations(userName);
        this.adapter.updateReservations(this.reservations);
        this.selectionPosition = -1;
    }

    private void deleteReservation(int reservationID){
        Reservation reservation = this.reservations.get(reservationID);
        this.bService.cancelReservation(userName, reservation.getID());
        this.reservations = this.bService.getReservations(userName);
        this.adapter.updateReservations(this.reservations);
        this.selectionPosition = -1;
    }

    public boolean onSupportNavigateUp(){
        this.onBackPressed();
        return true;
    }

    class ReservationClickListener implements AdapterView.OnItemClickListener {
        private Context parent;
        public ReservationClickListener(Context parent){
            super();
            this.parent = parent;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectionPosition = position;

            AlertDialog.Builder resAccepted = new AlertDialog.Builder(this.parent);
            resAccepted.setTitle("Edit reservation");
            resAccepted.setMessage("Do you want to accept or delete your reservation?");
            resAccepted.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteReservation(selectionPosition);
                }
            });
            Reservation res = reservations.get(position);
            if (!res.getStatus()) {
                resAccepted.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmReservation(selectionPosition);
                    }
                });
            }

            resAccepted.setNeutralButton("Back", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            resAccepted.show();
        }
    }
}