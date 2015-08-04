package ba.glaboratory.adem.mypizza;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MenuActivity extends Activity {

    String vrsta;
    String opis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        generateTable();
    }

    private void generateTable() {
        String url = "http://hci032.app.fit.ba/getMenu.php";
        final ListView listView1 = (ListView) findViewById(R.id.listView1);

        try {
            JSONArray data = new JSONArray(getJSONUrl(url));
            final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;

            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);

                map = new HashMap<String, String>();
                map.put("vrsta", c.getString("vrsta"));
                map.put("velicina", c.getString("velicina"));
                map.put("cijena", c.getString("cijena"));
                map.put("opis", c.getString("opis"));
                map.put("KM", " KM");
                MyArrList.add(map);
            }

            SimpleAdapter sAdapter;
            sAdapter = new SimpleAdapter(MenuActivity.this, MyArrList, R.layout.activity_menu_column,
                    new String[]{"vrsta", "velicina", "cijena", "KM"}, new int[]{R.id.vrstaPizze, R.id.velicinaPizze, R.id.cijenaPizze, R.id.cijenaKM});
            listView1.setAdapter(sAdapter);

            final AlertDialog.Builder viewDetail = new AlertDialog.Builder(this);
            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView myAdapter, View view, int position, long myLong) {
                    vrsta = MyArrList.get(position).get("vrsta").toString();
                    opis = MyArrList.get(position).get("opis").toString();

                    viewDetail.setIcon(R.drawable.ic_pizza);
                    viewDetail.setTitle(vrsta);
                    viewDetail.setMessage(opis);
                    viewDetail.setPositiveButton(getResources().getString(R.string.alertDialogPositiveButton),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                    dialog.dismiss();
                                }
                            });
                    viewDetail.show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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
