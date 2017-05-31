package funky.pom16.funkyreservation;

import android.app.AlertDialog;
import android.content.Context;
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

public class LoginActivity extends AppCompatActivity {

    Context context;
    IBackendService backend;
    private EditText username, password;
    private String loginResult;

    public static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        backend = new BackendMock();
        username = (EditText) findViewById(R.id.usernameEditText);
        password = (EditText) findViewById(R.id.passwordEditText);

        // Check if the user is already logged in:
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getString("username", "").length()!=0)
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void userLogin(View view){

        if(!validate())
            return;

        loginResult = backend.loginUser(username.getText().toString(),password.getText().toString());

        if (loginResult.equals("tokenForEverything"))
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();

            //Save preferences (keep the user logged in):
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", username.getText().toString());
            editor.apply();
        }
        else
        {
            AlertDialog.Builder resAccepted = new AlertDialog.Builder(context);

            resAccepted.setTitle(loginResult);
            resAccepted.setMessage("You have entered an invalid username or password!");

            resAccepted.setNegativeButton("Create a new account", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                    startActivity(intent);
                }
            });
            resAccepted.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            resAccepted.setIcon(android.R.drawable.ic_dialog_alert);
            resAccepted.show();
        }
    }

    public void userRegister(View view){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private boolean validate(){
        if(username.getText().toString().trim().isEmpty()){
            username.setError("Username cannot be empty");
            return false;
        }
        if(password.getText().toString().trim().isEmpty()){
            password.setError("Password cannot be empty");
            return false;
        }
        return true;
    }
}