package com.example.jordan.divvyupv12;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {
    public static Button button_sbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        OnClickButtonListener();
    }

    public void OnClickButtonListener() {
        button_sbm = (Button)findViewById(R.id.Create_Account_Button);
        button_sbm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText userName = (EditText) findViewById(R.id.Username_Field);
                        EditText userPassword = (EditText) findViewById(R.id.editText);
                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8)
                        {//allow execution of network connection on the main thread
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);


                            try { //surround all of this in a try/catch to get any errors that show up
                                Uri.Builder builder = new Uri.Builder()
                                        .appendQueryParameter("username", userName.getText().toString())
                                        .appendQueryParameter("password", userPassword.getText().toString())
                                        .appendQueryParameter("db", "codedb");
                                String query = builder.build().getEncodedQuery();
                                System.out.println("The query: " + query);  //verify query string - yes, it is valid

                                URL url = new URL("http://cslinux.samford.edu/codedb/create.php?" + query);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                try {
                                    InputStream in = new BufferedInputStream(conn.getInputStream());
                                    Scanner httpin = new Scanner(in);
                                    while(httpin.hasNextLine()){
                                        System.out.println(httpin.nextLine());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                finally{
                                    conn.disconnect();
                                }


                                //conn.setReadTimeout(50000);
                                //conn.setConnectTimeout(50000);
                                //conn.setRequestMethod("GET"); //Use POST or GET?
                                //conn.setDoInput(true);
                                //conn.setDoOutput(false);

                                //writing to the network socket to send the request to the server
                                //OutputStream os = conn.getOutputStream();
                                //BufferedWriter writer = new BufferedWriter(
                                //        new OutputStreamWriter(os, "UTF-8"));
                                //writer.write(query);
                                //writer.flush();
                                //writer.close();
                                //os.close();

                                //conn.connect();

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(".LoginActivity");
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
