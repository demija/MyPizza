package ba.glaboratory.adem.mypizza;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends ActionBarActivity {

    String ime;
    String prezime;
    String username;
    String password;
    String email;
    String brojtelefona;

    EditText imeEditText;
    EditText prezimeEditText;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;
    EditText emailEditText;
    EditText brojtelefonaEditText;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        imeEditText = (EditText) findViewById(R.id.firstnameText);
        prezimeEditText = (EditText) findViewById(R.id.lastnameText);
        usernameEditText = (EditText) findViewById(R.id.usernameText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);
        passwordConfirmEditText = (EditText) findViewById(R.id.passwordConfirmText);
        emailEditText = (EditText) findViewById(R.id.emailText);
        brojtelefonaEditText = (EditText) findViewById(R.id.mobileNumberText);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        brojtelefonaEditText.setText(telephonyManager.getLine1Number());

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkRegistration() && checkPassword()) {
                    generateValues();
                    new InsertUser().execute();
                    //insertUser();
                }
            }
        });
    }

    private boolean checkRegistration() {
        boolean valid = true;

        if (imeEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (prezimeEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (usernameEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (passwordEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (passwordConfirmEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (emailEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (brojtelefonaEditText.getText().toString().matches("")) {
            valid = false;
        }

        if (!valid) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastRegisterError), Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private boolean checkPassword() {
        boolean valid = true;

        if (!passwordEditText.getText().toString().matches(passwordConfirmEditText.getText().toString())) {
            valid = false;
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastRegisterPassError), Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private void generateValues() {
        ime = imeEditText.getText().toString();
        prezime = prezimeEditText.getText().toString();
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        email = emailEditText.getText().toString();
        brojtelefona = brojtelefonaEditText.getText().toString();
    }

    public class InsertUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://hci032.app.fit.ba/insertUser.php");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("ime", ime));
                nameValuePairs.add(new BasicNameValuePair("prezime", prezime));
                nameValuePairs.add(new BasicNameValuePair("username", username));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("brojtelefona", brojtelefona));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {

            } catch (IOException e) {
            }

            return true;
        }

        protected void onPostExecute(Boolean poruka){
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastRegisterOK), Toast.LENGTH_SHORT).show();

            clearInput();

            finish();
        }
    }

    private void clearInput() {
        imeEditText.setText("");
        prezimeEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
        emailEditText.setText("");
    }
}
