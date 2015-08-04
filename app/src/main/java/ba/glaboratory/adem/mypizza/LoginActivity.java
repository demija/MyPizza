package ba.glaboratory.adem.mypizza;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import ba.glaboratory.adem.model.KorisnikClass;

public class LoginActivity extends ActionBarActivity {

    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    TextView registerLink;
    HttpPost httppost;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;
    String userid;

    boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cd = new ConnectionDetector(getApplicationContext());

        usernameEditText = (EditText) findViewById(R.id.usernameText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    if (validateInput()) {
                        dialog = ProgressDialog.show(LoginActivity.this, "", getResources().getString(R.string.alertDialogLogin), true);
                        new Thread(new Runnable() {
                            public void run() {
                                login();
                            }
                        }).start();
                    }
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastNoInternet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerLink = (TextView) findViewById(R.id.registerLink);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInput() {
        boolean valid = true;

        if (usernameEditText.getText().toString().matches("")) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastInsertUsername), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (passwordEditText.getText().toString().matches("")) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastInsertPass), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void login() {
        try {
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://hci032.app.fit.ba/loginUser.php");

            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", usernameEditText.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("password", passwordEditText.getText().toString().trim()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            response = httpclient.execute(httppost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);
            System.out.println("Response : " + response);
            runOnUiThread(new Runnable() {
                public void run() {
                    dialog.dismiss();
                }
            });

            if (!response.equalsIgnoreCase("No Such User Found")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        userid = response.toString();

                        KorisnikClass.korisnikId = userid;

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                showAlert();
            }

        } catch (Exception e) {
            dialog.dismiss();
            System.out.println("Exception : " + e.getMessage());
        }
    }

    private void showAlert() {
        LoginActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setIcon(R.drawable.ic_error);
                builder.setTitle(getResources().getString(R.string.alertDialogLoginErrorTitle));
                builder.setMessage(getResources().getString(R.string.alertDialogLoginErrorMessage))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.alertDialogPositiveButton), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
