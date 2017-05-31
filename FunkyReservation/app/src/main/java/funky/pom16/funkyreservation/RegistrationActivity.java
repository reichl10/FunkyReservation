package funky.pom16.funkyreservation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import funky.pom16.funkyreservation.backend.IBackendService;
import funky.pom16.funkyreservation.backend.connection.BackendMock;

public class RegistrationActivity extends AppCompatActivity {

    IBackendService backend;
    EditText user, first, last, password;
    public static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        backend = new BackendMock();

        user = (EditText) findViewById(R.id.usernameEditText);
        first = (EditText) findViewById(R.id.firstEditText);
        last = (EditText) findViewById(R.id.lastEditText);
        password = (EditText) findViewById(R.id.passwordEditText);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void registerUser(View view){

        boolean validInput = validate();
        if(!validInput)
            return;

        String token = backend.register(user.getText().toString(), password.getText().toString(),
                first.getText().toString(), last.getText().toString());

        if(token != null && !token.isEmpty()){
            backend.loginUser(user.getText().toString(),password.getText().toString());

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            //Save preferences (keep the user logged in):
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", user.getText().toString());
            editor.apply();
        } else {
            AlertDialog.Builder resAccepted = new AlertDialog.Builder(getBaseContext());

            resAccepted.setTitle("Registration failed");
            resAccepted.setMessage("Registration failed due to unknown reason!");

            resAccepted.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            resAccepted.setIcon(android.R.drawable.ic_dialog_alert);
            resAccepted.show();
        }
    }

    public void back(View view){
        this.onBackPressed();
    }

    private boolean validate(){
        boolean checkFirst = true, checkLast = true, checkUser = true, checkPass = true;
        if(first.getText().toString().trim().isEmpty()){
            first.setError("First name cannot be empty");
            checkFirst = false;
        }
        if(last.getText().toString().trim().isEmpty()){
            last.setError("Last name cannot be empty");
            checkLast = false;
        }
        if(user.getText().toString().trim().isEmpty()){
            user.setError("Username cannot be empty");
            checkUser = false;
        }
        if(password.getText().toString().trim().isEmpty()){
            password.setError("Password cannot be empty");
            checkPass = false;
        }
        return checkFirst && checkLast && checkUser && checkPass;
    }

    public void onHomeClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
