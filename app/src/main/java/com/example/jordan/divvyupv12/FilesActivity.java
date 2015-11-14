package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class FilesActivity extends AppCompatActivity {
    String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        String json = "";
        JSONArray arr = new JSONArray();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {//allow execution of network connection on the main thread
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            System.out.println("CURRENT USER ID IS " + Globals.getInstance().userID);
            try { //must surround with try/catch to filter errors
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("userid", Globals.getInstance().userID)
                        .appendQueryParameter("db", "codedb");
                String query = builder.build().getEncodedQuery();
                //System.out.println("The query: " + query);      //to verify query string

                URL url = new URL("http://cslinux.samford.edu/codedb/getfilelist.php?" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                System.out.println("The complete url: " + url); //to verify full url
                try {//to get the response from server
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    Scanner httpin = new Scanner(in);
                    for(int i = 0; httpin.hasNextLine(); i++) {
                        json += httpin.nextLine();
                    }
                    arr = new JSONArray(json);
                    System.out.println("The JSON array contents: " + arr.toString());
                    if(arr.length() == 0) { //if the json array is empty, then this user does not have any files
                        Toast.makeText(FilesActivity.this, "You do not have any files yet.", Toast.LENGTH_LONG).show();
                    } else {                //user exists
                        JSONObject obj;
                        String[] files = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            obj = arr.getJSONObject(i);
                            System.out.println("The filename is is: " + obj.getString("filename"));
                            files[i] = obj.getString("filename");
                        }
                        ArrayAdapter<String> filesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,files);
                        ListView listView = (ListView) findViewById(R.id.listView);
                        listView.setAdapter(filesAdapter);
                    }//password for db is
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{//disconnect after making the connection and executing the query
                    conn.disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //go to hub page if the login was successful
        //if(arr.length() > 0) {
        //    Intent intent2 = new Intent("com.example.jordan.divvyupv12.HubActivity");
         //   startActivity(intent2);
        //}
        //items = new String[]{"kahdsk", "hkasgd","kjasgdjc","asjkhd"};
        //ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        //ListView listView = (ListView) findViewById(R.id.items);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
