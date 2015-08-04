package ba.glaboratory.adem.mypizza;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ba.glaboratory.adem.model.KorisnikClass;

public class OrderActivity extends Activity {

    String korisnikid;
    String narudzbaid;
    String vrsta;
    String opis;
    String velicina;
    String cijena;
    String adresa;
    String kontakt;

    TextView cijenaText;
    TextView imePrezimeText;
    EditText adresaEditText;
    EditText brojtelefonaEditText;
    Button finishOrderButton;

    HttpClient httpclient;
    HttpPost httppost;
    HttpResponse response;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    final Context context = this;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        cijenaText = (TextView) findViewById(R.id.cijenaText);
        imePrezimeText = (TextView) findViewById(R.id.imePrezimeText);
        adresaEditText = (EditText) findViewById(R.id.adresaText);
        brojtelefonaEditText = (EditText) findViewById(R.id.mobileNumberText);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        brojtelefonaEditText.setText(telephonyManager.getLine1Number());

        korisnikid = KorisnikClass.korisnikId;

        generateOrdersList();

        finishOrderButton = (Button) findViewById(R.id.finishOrderButton);
        finishOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new finishOrder().execute();
            }
        });
    }

    private void generateOrdersList() {
        try {
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost("http://hci032.app.fit.ba/getOrders.php");

            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("korisnikid", korisnikid));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            response = httpclient.execute(httppost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);
            System.out.println("Response : " + response);

            if (!response.equalsIgnoreCase("No Such User Found")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        final ListView listView1 = (ListView) findViewById(R.id.listView1);

                        try {
                            JSONArray data = new JSONArray(response.trim());
                            final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
                            HashMap<String, String> map;

                            int temp = 0;

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject c = data.getJSONObject(i);

                                map = new HashMap<String, String>();
                                map.put("narudzbaid", c.getString("narudzbaid"));
                                map.put("korisnikid", c.getString("korisnikid"));
                                map.put("ime", c.getString("ime"));
                                map.put("prezime", c.getString("prezime"));
                                map.put("pizzaid", c.getString("pizzaid"));
                                map.put("vrstaid", c.getString("vrstaid"));
                                map.put("velicinaid", c.getString("velicinaid"));
                                map.put("cijena", c.getString("cijena"));
                                map.put("vrsta", c.getString("vrsta"));
                                map.put("opis", c.getString("opis"));
                                map.put("velicina", c.getString("velicina"));
                                map.put("statusid", c.getString("statusid"));
                                map.put("KM", " KM");
                                MyArrList.add(map);

                                temp += Integer.parseInt(c.getString("cijena"));
                            }

                            SimpleAdapter sAdapter;
                            sAdapter = new SimpleAdapter(OrderActivity.this, MyArrList, R.layout.activity_order_column,
                                    new String[]{"vrsta", "velicina", "cijena", "KM"}, new int[]{R.id.vrstaPizze, R.id.velicinaPizze, R.id.cijenaPizze, R.id.cijenaKM});
                            listView1.setAdapter(sAdapter);

                            cijenaText.setText("Ukupno: " + String.valueOf(temp) + " KM");
                            imePrezimeText.setText(MyArrList.get(0).get("ime") + " " + MyArrList.get(0).get("prezime"));

                            final AlertDialog.Builder viewDetail = new AlertDialog.Builder(context);
                            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView myAdapter, View view, int position, long myLong) {
                                    vrsta = MyArrList.get(position).get("vrsta").toString();
                                    opis = MyArrList.get(position).get("opis").toString();
                                    velicina = MyArrList.get(position).get("velicina").toString();
                                    cijena = MyArrList.get(position).get("cijena").toString();
                                    narudzbaid = MyArrList.get(position).get("narudzbaid").toString();

                                    viewDetail.setIcon(R.drawable.ic_pizza);
                                    viewDetail.setTitle(vrsta);
                                    viewDetail.setMessage(getResources().getString(R.string.alertDialogPizzaOpis) + " " + opis + '\n'
                                            + getResources().getString(R.string.alertDialogPizzaVelicina) + " " + velicina + '\n'
                                            + getResources().getString(R.string.alertDialogPizzaCijena) + " " + cijena + " KM" + '\n');
                                    viewDetail.setPositiveButton(getResources().getString(R.string.alertDialogPositiveButton),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    // TODO Auto-generated method stub
                                                    dialog.dismiss();
                                                }
                                            });
                                    viewDetail.setNegativeButton(getResources().getString(R.string.alertDialogNegativeButton),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    deleteOrder();
                                                    dialog.dismiss();
                                                }
                                            }
                                    );
                                    viewDetail.show();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Toast.makeText(OrderActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            //dialog.dismiss();
            System.out.println("Exception : " + e.getMessage());
        }
    }

    private void deleteOrder() {
        final AlertDialog.Builder viewDetail = new AlertDialog.Builder(context);
        viewDetail.setIcon(R.drawable.ic_help);
        viewDetail.setTitle(getResources().getString(R.string.alertDialogOrderDelete));
        viewDetail.setMessage(getResources().getString(R.string.alertDialogOrderDeleteMessage));
        viewDetail.setPositiveButton(getResources().getString(R.string.alertDialogNegativeButton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        try {
                            httpclient = new DefaultHttpClient();
                            httppost = new HttpPost("http://hci032.app.fit.ba/deleteOrder.php");

                            nameValuePairs = new ArrayList<NameValuePair>(2);
                            nameValuePairs.add(new BasicNameValuePair("narudzbaid", narudzbaid));
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                            response = httpclient.execute(httppost);

                            ResponseHandler<String> responseHandler = new BasicResponseHandler();
                            final String response = httpclient.execute(httppost, responseHandler);
                            System.out.println("Response : " + response);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    //dialog.dismiss();
                                }
                            });

                            if (response.equalsIgnoreCase("Record deleted successfully")) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderDeleteConfirm), Toast.LENGTH_SHORT).show();
                                        generateOrdersList();
                                    }
                                });
                            } else {
                                Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderDeleteError), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            dialog.dismiss();
                            System.out.println("Exception : " + e.getMessage());
                        }

                        dialog.dismiss();
                    }
                });
        viewDetail.setNegativeButton(getResources().getString(R.string.alertDialogOrderDeleteNo),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );
        viewDetail.show();
    }

    public class finishOrder extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {

            if (valdateInput()) {
                try {
                    adresa = adresaEditText.getText().toString();
                    kontakt = brojtelefonaEditText.getText().toString();

                    httpclient = new DefaultHttpClient();
                    httppost = new HttpPost("http://hci032.app.fit.ba/finishOrder.php");

                    nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("korisnikid", korisnikid));
                    nameValuePairs.add(new BasicNameValuePair("kontakt", kontakt));
                    nameValuePairs.add(new BasicNameValuePair("adresa", adresa));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    response = httpclient.execute(httppost);

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    final String response = httpclient.execute(httppost, responseHandler);
                    System.out.println("Response : " + response);

                    if (response.equalsIgnoreCase("Record edited successfully")) {
                        Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderMoreOK), Toast.LENGTH_SHORT).show();
                        //finish();
                    } else {
                        Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderError), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    System.out.println("Exception : " + e.getMessage());
                }
            }

            return true;
        }

        protected void onPostExecute(Boolean poruka){
            finish();
        }
    }

    private boolean valdateInput() {
        boolean valid = true;

        if (adresaEditText.getText().toString().matches("")) {
            valid = false;
            Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderAddressError), Toast.LENGTH_SHORT).show();
        }

        if (brojtelefonaEditText.getText().toString().matches("")) {
            valid = false;
            Toast.makeText(OrderActivity.this, getResources().getString(R.string.toastOrderNumberError), Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
