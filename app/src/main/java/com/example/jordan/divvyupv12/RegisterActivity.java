package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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
                        EditText userPassword = (EditText) findViewById(R.id.Password_Field);
                        String responseString = "";
                        Boolean response = false;
                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8)
                        {//allow execution of network connection on the main thread
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            try { //must surround with try/catch to filter errors
                                Uri.Builder builder = new Uri.Builder()
                                        .appendQueryParameter("username", userName.getText().toString())
                                        .appendQueryParameter("password", userPassword.getText().toString())
                                        .appendQueryParameter("db", "codedb");
                                String query = builder.build().getEncodedQuery();
                                System.out.println("The query: " + query);      //to verify query string

                                URL url = new URL("http://cslinux.samford.edu/codedb/create.php?" + query);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                System.out.println("The complete url: " + url); //to verify full url
                                try {//to get the response from server
                                    InputStream in = new BufferedInputStream(conn.getInputStream());
                                    Scanner httpin = new Scanner(in);
                                    while(httpin.hasNextLine()){
                                        responseString = httpin.nextLine();
                                        if (responseString.trim().equalsIgnoreCase("true") || responseString.trim().equalsIgnoreCase("false")) {
                                            response = Boolean.valueOf(responseString);
                                        } else {
                                            System.out.println("The response was not the expected boolean value.");
                                            response = null;
                                        }
                                        if(response == true){           //true - user was added, display message and continue
                                            System.out.println("Response is " + response);
                                            Toast.makeText(RegisterActivity.this, "Your account was created successfully!", Toast.LENGTH_LONG).show();
                                        } else if (response == false){                        //false - error occurred or username already exists, do not continue
                                            System.out.println("Response is " + response);
                                            AlertDialog.Builder myAlert = new AlertDialog.Builder(RegisterActivity.this);
                                            myAlert.setMessage("Oops, an error occurred! \n\nThe most likely cause is that your username may already be taken. " +
                                                    "\n\nPlease try creating an account again, but this time with a new username. Just click the" +
                                                    " back button on your device and type in new information in the account creation fields.").create();
                                            myAlert.show();
                                        } else {
                                            System.out.println("The response was not a boolean value.");
                                        }
                                    }
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
                        //continue by switching back to the main login page after user creation
                        if(response == true) {
                            Intent intent = new Intent(".LoginActivity");
                            startActivity(intent);
                        }
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
