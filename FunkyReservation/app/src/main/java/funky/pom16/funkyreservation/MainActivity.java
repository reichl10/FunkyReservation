package funky.pom16.funkyreservation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;

public class MainActivity extends AppCompatActivity {

    public static String userName;
    final String HOCKEYAPP_ID = "e2e7e96016cb41b0ada0802cca44341f ";
    TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = LoginActivity.prefs.getString("username", "");
        welcome = (TextView) findViewById(R.id.welcome);
        welcome.setText("Welcome " + userName + "!");
        checkForUpdates();
    }

    public void findARestaurantList(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void seeReservations(View view){
        Intent intent = new Intent(this, ReservationsView.class);
        startActivity(intent);
    }

    public void logOut(View view)
    {
        SharedPreferences.Editor editor = LoginActivity.prefs.edit();
        editor.remove("username");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void checkForCrashes() {
        CrashManager.register(this, HOCKEYAPP_ID);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, HOCKEYAPP_ID);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    public void sendFeedback(View view) {
        FeedbackManager.register(this, HOCKEYAPP_ID, null);
        FeedbackManager.showFeedbackActivity(this);
    }
}
