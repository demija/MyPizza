package ba.glaboratory.adem.mypizza;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ba.glaboratory.adem.adapter.TitleNavigationAdapter;
import ba.glaboratory.adem.model.KorisnikClass;
import ba.glaboratory.adem.model.SpinnerNavItem;

public class MainActivity extends Activity implements ActionBar.OnNavigationListener {

    private ArrayList<SpinnerNavItem> navSpinner;
    private TitleNavigationAdapter adapter;

    String vrstaId;
    String vrsta;
    String opis;
    String velicinaId;
    String velicina;
    String korisnikid;
    String cijenanarudzbe = "";
    String pizzaid;
    String statusid;

    TextView pizzaOpis;
    TextView pizzaCijena;
    Button orderMoreButton;
    Button finishOrderButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setTitle("");

        navSpinner = new ArrayList<SpinnerNavItem>();
        navSpinner.add(new SpinnerNavItem("Pizza", R.drawable.ic_pizza));
        navSpinner.add(new SpinnerNavItem("Menu", R.drawable.ic_menu));
        navSpinner.add(new SpinnerNavItem("Narudžbe", R.drawable.ic_order));
        navSpinner.add(new SpinnerNavItem("Odjava", R.drawable.ic_door));

        adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);

        actionBar.setListNavigationCallbacks(adapter, this);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        korisnikid = KorisnikClass.korisnikId;

        pizzaOpis = (TextView) findViewById(R.id.pizzaOpis);
        pizzaCijena = (TextView) findViewById(R.id.pizzaCijena);
        pizzaCijena.setText("Cijena: ");

        generatePizzaVrsteSpinner();

        generatePizzaSizesSpinner();

        orderMoreButton = (Button) findViewById(R.id.orderMoreButton);
        orderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vrstaId.toString().equals("1") || velicinaId.toString().equals("1")) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toastOrderMoreError), Toast.LENGTH_SHORT).show();
                } else {
                    new createOrder().execute();
                }
            }
        });

        finishOrderButton = (Button) findViewById(R.id.finishOrderButton);
        finishOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                startActivity(intent);
            }
        });
    }

    private void generatePizzaVrsteSpinner() {
        final Spinner spin = (Spinner) findViewById(R.id.pizzaVrstaSpinner);
        String url = "http://hci032.app.fit.ba/getVrste.php";

        try {
            JSONArray data = new JSONArray(getJSONUrl(url));

            final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;

            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);

                map = new HashMap<String, String>();
                map.put("vrstaid", c.getString("vrstaid"));
                map.put("vrsta", c.getString("vrsta"));
                map.put("opis", c.getString("opis"));
                MyArrList.add(map);
            }

            SimpleAdapter sAdap;
            sAdap = new SimpleAdapter(MainActivity.this, MyArrList, R.layout.activity_show_vrste,
                    new String[]{"vrsta"}, new int[]{R.id.vrstaid});
            spin.setAdapter(sAdap);

            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                    vrstaId = MyArrList.get(position).get("vrstaid").toString();
                    vrsta = MyArrList.get(position).get("vrsta").toString();
                    opis = MyArrList.get(position).get("opis").toString();

                    showPizzaDetails();
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                    Toast.makeText(MainActivity.this, "Your Selected : Nothing", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void generatePizzaSizesSpinner() {
        final Spinner spin = (Spinner) findViewById(R.id.pizzaVelicinaSpinner);
        String url = "http://hci032.app.fit.ba/getVelicine.php";

        try {
            JSONArray data = new JSONArray(getJSONUrl(url));

            final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;

            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);

                map = new HashMap<String, String>();
                map.put("velicinaid", c.getString("velicinaid"));
                map.put("velicina", c.getString("velicina"));
                MyArrList.add(map);
            }

            SimpleAdapter sAdap;
            sAdap = new SimpleAdapter(MainActivity.this, MyArrList, R.layout.activity_show_velicine,
                    new String[]{"velicina"}, new int[]{R.id.velicinaid});
            spin.setAdapter(sAdap);

            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                    velicinaId = MyArrList.get(position).get("velicinaid").toString();
                    velicina = MyArrList.get(position).get("velicina").toString();

                    showPizzaDetails();
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                    Toast.makeText(MainActivity.this, "Your Selected : Nothing", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showPizzaDetails() {
        getPizzaId();

        final AlertDialog.Builder viewDetail = new AlertDialog.Builder(this);
        viewDetail.setIcon(R.drawable.ic_pizza);
        viewDetail.setTitle(vrsta);
        viewDetail.setMessage(getResources().getString(R.string.alertDialogPizzaOpis) + " " + opis + "\n"
                + getResources().getString(R.string.alertDialogPizzaVelicina) + " " + velicina + "\n"
                + getResources().getString(R.string.alertDialogPizzaCijena) + " " + cijenanarudzbe + " KM" + "\n");
        viewDetail.setPositiveButton(getResources().getString(R.string.alertDialogPositiveButton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });

        pizzaOpis.setText(opis.toString());
        pizzaCijena.setText(getResources().getString(R.string.alertDialogPizzaCijena) + " " + cijenanarudzbe + " KM");

        if (!vrsta.equals("Odaberi") && !velicina.equals("Odaberi")) {
            viewDetail.show();
        }
    }

    public String getJSONUrl(String url) {
        StringBuilder str = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) { // Download OK
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
            } else {
                Log.e("Log", "Failed to download result..");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str.toString();
    }

    private void getPizzaId() {
        if (vrstaId.toString().equals("2")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "6";
                pizzaid = "1";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "8";
                pizzaid = "2";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "16";
                pizzaid = "3";
            }
        } else if (vrstaId.toString().equals("3")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "6";
                pizzaid = "4";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "8";
                pizzaid = "5";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "16";
                pizzaid = "6";
            }
        } else if (vrstaId.toString().equals("4")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "6";
                pizzaid = "7";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "8";
                pizzaid = "8";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "16";
                pizzaid = "9";
            }
        } else if (vrstaId.toString().equals("5")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "6";
                pizzaid = "10";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "8";
                pizzaid = "11";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "16";
                pizzaid = "12";
            }
        } else if (vrstaId.toString().equals("6")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "6";
                pizzaid = "13";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "8";
                pizzaid = "14";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "16";
                pizzaid = "15";
            }
        } else if (vrstaId.toString().equals("7")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "7";
                pizzaid = "16";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "9";
                pizzaid = "17";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "18";
                pizzaid = "18";
            }
        } else if (vrstaId.toString().equals("8")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "7";
                pizzaid = "19";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "9";
                pizzaid = "20";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "18";
                pizzaid = "21";
            }
        } else if (vrstaId.toString().equals("9")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "7";
                pizzaid = "22";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "9";
                pizzaid = "23";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "18";
                pizzaid = "24";
            }
        } else if (vrstaId.toString().equals("10")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "7";
                pizzaid = "25";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "9";
                pizzaid = "26";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "18";
                pizzaid = "27";
            }
        } else if (vrstaId.toString().equals("11")) {
            if (velicinaId.toString().equals("2")) {
                cijenanarudzbe = "7";
                pizzaid = "30";
            } else if (velicinaId.toString().equals("3")) {
                cijenanarudzbe = "9";
                pizzaid = "31";
            } else if (velicinaId.toString().equals("4")) {
                cijenanarudzbe = "18";
                pizzaid = "32";
            }
        }
    }

    public class createOrder extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://hci032.app.fit.ba/createOrder.php");
            statusid = "1";

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("korisnikid", korisnikid));
                nameValuePairs.add(new BasicNameValuePair("pizzaid", pizzaid));
                nameValuePairs.add(new BasicNameValuePair("cijenanarudzbe", cijenanarudzbe));
                nameValuePairs.add(new BasicNameValuePair("statusid", statusid));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {

            } catch (IOException e) {
            }

            return true;
        }

        protected void onPostExecute(Boolean poruka){

            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastOrderMoreOK), Toast.LENGTH_SHORT).show();

            final Spinner spinVrsta = (Spinner) findViewById(R.id.pizzaVrstaSpinner);
            spinVrsta.setSelection(0);
            final Spinner spinVelicina = (Spinner) findViewById(R.id.pizzaVelicinaSpinner);
            spinVelicina.setSelection(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // Action to be taken after selecting a spinner item

        switch (itemPosition) {
            case 0:
                break;

            case 1:
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
                break;

            case 2:
                Intent intent1 = new Intent(getApplicationContext(), OrderActivity.class);
                startActivity(intent1);
                break;

            case 3:
                finish();
                break;
        }

        return false;
    }
}
