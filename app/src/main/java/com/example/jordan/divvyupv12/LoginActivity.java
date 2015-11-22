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

public class LoginActivity extends AppCompatActivity{
    public static Button login_button , register_now_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

              OnClickButtonListener();
    }

    public void OnClickButtonListener() {
        register_now_button = (Button)findViewById(R.id.register_now);
        register_now_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.example.jordan.divvyupv12.RegisterActivity");
                        startActivity(intent);
                    }
                }
        );
        login_button = (Button) findViewById(R.id.Login_Button);
        login_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText userName = (EditText) findViewById(R.id.Login_Username_Field);
                        EditText userPassword = (EditText) findViewById(R.id.Login_Password_Field);
                        String json = "";
                        JSONArray arr = new JSONArray();
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

                                URL url = new URL("http://cslinux.samford.edu/codedb/login.php?" + query);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                System.out.println("The complete url: " + url); //to verify full url
                                try {//to get the response from server
                                    InputStream in = new BufferedInputStream(conn.getInputStream());
                                    Scanner httpin = new Scanner(in);
                                    while(httpin.hasNextLine()) {
                                        json += httpin.nextLine();
                                    }
                                    arr = new JSONArray(json);
                                    System.out.println("The JSON array contents: " + arr.toString());
                                    if(arr.length() == 0) { //if the json array is empty, then this user does not exist in the database
                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(LoginActivity.this);
                                        myAlert.setMessage("Login Failed. \n\nThe username or password may be incorrect." +
                                                "\n\nIf you do not have an account, please create one first, then try again!").create();
                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        myAlert.show();
                                    } else {                //user exists; get the json object inside the array and store user_id variable
                                        JSONObject obj = arr.getJSONObject(0);
                                        System.out.println("The user_id is: " + obj.getString("id")); //brandon the "obj.getString('id')" gets the value of user_id
                                        Globals.getInstance().userID = obj.getString("id");//stores the user id from the database in the userID global variable
                                        Toast.makeText(LoginActivity.this, "Welcome " + userName.getText().toString() + "!", Toast.LENGTH_LONG).show();
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
                        //go to hub page if the login was successful
                        if(arr.length() > 0) {
                            Intent intent2 = new Intent("com.example.jordan.divvyupv12.HubActivity");
                            startActivity(intent2);
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            Toast.makeText(LoginActivity.this, "Feature Coming Soon!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
