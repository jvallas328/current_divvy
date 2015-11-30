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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CreateFileActivity extends AppCompatActivity {
    public static Button create_file_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_file2);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText fileName = (EditText) findViewById(R.id.fileName);
                EditText fileContents = (EditText) findViewById(R.id.fileContents);
                String json = "";
                JSONArray arr = new JSONArray();
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {//allow execution of network connection on the main thread
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try { //must surround with try/catch to filter errors
                        Uri.Builder builder = new Uri.Builder()

                                .appendQueryParameter("userid", Globals.getInstance().userID)
                                .appendQueryParameter("filename", fileName.getText().toString())
                                .appendQueryParameter("textcontent", fileContents.getText().toString())
                                .appendQueryParameter("db", "codedb");
                        String query = builder.build().getEncodedQuery();
                        System.out.println("The query: " + query);      //to verify query string
                        URL url = new URL("http://cslinux.samford.edu/codedb/addfile.php?" + query);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        System.out.println("The complete url: " + url); //to verify full url
                        try {//to get the response from server
                            InputStream in = new BufferedInputStream(conn.getInputStream());
                            Scanner httpin = new Scanner(in);
                            //System.out.println("The id of the created file is " + httpin.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {//disconnect after making the connection and executing the query
                            conn.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                startActivity(new Intent(CreateFileActivity.this, FilesActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_file, menu);
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
