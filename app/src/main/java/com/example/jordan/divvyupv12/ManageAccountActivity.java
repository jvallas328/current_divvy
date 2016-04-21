package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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

public class ManageAccountActivity extends AppCompatActivity {
    public static Button delete_account_button, save_info_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        OnClickButtonListener();

        //get the current account information
        String json = "";
        JSONArray arr;
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {//allow execution of network connection on the main thread
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try { //must surround with try/catch to filter errors
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("userid", Globals.getInstance().userID)
                        .appendQueryParameter("db", "codedb");
                String query = builder.build().getEncodedQuery();
                System.out.println("The query: " + query);      //to verify query string

                URL url = new URL("http://cslinux.samford.edu/codedb/loadaccount.php?" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                System.out.println("The complete url: " + url); //to verify full url
                try {//to get the response from server
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    Scanner httpin = new Scanner(in);
                    while (httpin.hasNextLine()) {
                        json += httpin.nextLine();
                    }
                    arr = new JSONArray(json);
                    System.out.println("The JSON array contents: " + arr.toString());

                    JSONObject obj = arr.getJSONObject(0);
                    System.out.println("The user_id is: " + obj.getString("id")); //brandon the "obj.getString('id')" gets the value of user_id
                    String username = obj.getString("username");//stores the user id from the database in the userID global variable
                    String firstName = obj.getString("firstName");
                    String lastName = obj.getString("lastName");
                    String age = obj.getString("age");
                    String country = obj.getString("country");
                    String bio = obj.getString("bio");

                    //display current username
                    EditText usernameEditText = (EditText) findViewById(R.id.Username_Show_Field);
                    usernameEditText.setText(username);
                    usernameEditText.setClickable(false);
                    usernameEditText.setCursorVisible(false);
                    usernameEditText.setFocusable(false);
                    usernameEditText.setFocusableInTouchMode(false);

                    //display current first name data
                    EditText firstNameEditText = (EditText) findViewById(R.id.First_Name_Field);
                    if (firstName == "null") {
                        firstNameEditText.setText("No Data");
                    } else {
                        firstNameEditText.setText(firstName);
                    }

                    //display current last name data
                    EditText lastNameEditText = (EditText) findViewById(R.id.Last_Name_Field);
                    if (lastName == "null") {
                        lastNameEditText.setText("No Data");
                    } else {
                        lastNameEditText.setText(lastName);
                    }

                    //display current age data
                    EditText ageEditText = (EditText) findViewById(R.id.Age_Field);
                    if (age == "null") {
                        ageEditText.setText("No Data");
                    } else {
                        ageEditText.setText(age);
                    }
                    ageEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                    //display current country data
                    EditText countryEditText = (EditText) findViewById(R.id.Country_Field);
                    if (country == "null") {
                        countryEditText.setText("No Data");
                    } else {
                        countryEditText.setText(country);
                    }

                    //display current bio data
                    EditText bioEditText = (EditText) findViewById(R.id.Bio_Field);
                    if (bio == "null") {
                        bioEditText.setText("No Data");
                    } else {
                        bioEditText.setText(bio);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {//disconnect after making the connection and executing the query
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void OnClickButtonListener() {
        delete_account_button = (Button) findViewById(R.id.Delete_Account_Button);
        delete_account_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder myAlert1 = new AlertDialog.Builder(ManageAccountActivity.this);
                        myAlert1.setMessage("Are you sure you want to delete your account? \n\nThis will permanently delete your account information and " +
                                "owned files. Also, other users will no longer be able to access files you shared with them.").create();
                        myAlert1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String responseString = "";
                                Boolean response = false;
                                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                                if (SDK_INT > 8) {//allow execution of network connection on the main thread
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                            .permitAll().build();
                                    StrictMode.setThreadPolicy(policy);
                                    try { //must surround with try/catch to filter errors
                                        Uri.Builder builder = new Uri.Builder()
                                                .appendQueryParameter("username", Globals.getInstance().username)
                                                .appendQueryParameter("db", "codedb");
                                        String query = builder.build().getEncodedQuery();
                                        System.out.println("The query: " + query);      //to verify query string

                                        URL url = new URL("http://cslinux.samford.edu/codedb/deleteaccount.php?" + query);
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        System.out.println("The complete url: " + url); //to verify full url
                                        try {//to get the response from server
                                            InputStream in = new BufferedInputStream(conn.getInputStream());
                                            Scanner httpin = new Scanner(in);
                                            while (httpin.hasNextLine()) {
                                                responseString = httpin.nextLine();
                                                if (responseString.trim().equalsIgnoreCase("true") || responseString.trim().equalsIgnoreCase("false")) {
                                                    response = Boolean.valueOf(responseString);
                                                } else {
                                                    System.out.println("The response was not the expected boolean value.");
                                                    response = null;
                                                }
                                                if (response == true) {           //true - user's account was deleted, display message and continue
                                                    System.out.println("Response is " + response);
                                                    Intent intent1 = new Intent(".LoginActivity");
                                                    startActivity(intent1);
                                                    Thread.sleep(3000);
                                                    Toast.makeText(ManageAccountActivity.this, "Your account and files were deleted successfully.", Toast.LENGTH_LONG).show();
                                                } else if (response == false) {                        //false - error occurred or username already exists, do not continue
                                                    System.out.println("Response is " + response);
                                                    AlertDialog.Builder myAlert2 = new AlertDialog.Builder(ManageAccountActivity.this);
                                                    myAlert2.setMessage("Oops, an error occurred! Please try again.").create();
                                                    myAlert2.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    myAlert2.show();
                                                } else {
                                                    System.out.println("The response was not a boolean value.");
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {//disconnect after making the connection and executing the query
                                            conn.disconnect();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                dialog.dismiss();
                            }
                        });
                        myAlert1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        myAlert1.show();

                    }
                }
        );

        save_info_button = (Button) findViewById(R.id.Save_And_Exit_Button);
        save_info_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //get new account information
                        EditText firstNameEditText = (EditText) findViewById(R.id.First_Name_Field);
                        EditText lastNameEditText = (EditText) findViewById(R.id.Last_Name_Field);
                        EditText ageEditText = (EditText) findViewById(R.id.Age_Field);
                        EditText countryEditText = (EditText) findViewById(R.id.Country_Field);
                        EditText bioEditText = (EditText) findViewById(R.id.Bio_Field);

                        String responseString = "";
                        Boolean response = false;
                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8) {//allow execution of network connection on the main thread
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            try { //must surround with try/catch to filter errors
                                Uri.Builder builder = new Uri.Builder()
                                        .appendQueryParameter("userid", Globals.getInstance().userID)
                                        .appendQueryParameter("firstName", firstNameEditText.getText().toString())
                                        .appendQueryParameter("lastName", lastNameEditText.getText().toString())
                                        .appendQueryParameter("age", ageEditText.getText().toString())
                                        .appendQueryParameter("country", countryEditText.getText().toString())
                                        .appendQueryParameter("bio", bioEditText.getText().toString())
                                        .appendQueryParameter("db", "codedb");
                                String query = builder.build().getEncodedQuery();
                                System.out.println("The query: " + query);      //to verify query string

                                URL url = new URL("http://cslinux.samford.edu/codedb/editaccount.php?" + query);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                System.out.println("The complete url: " + url); //to verify full url
                                try {//to get the response from server
                                    InputStream in = new BufferedInputStream(conn.getInputStream());
                                    Scanner httpin = new Scanner(in);
                                    while (httpin.hasNextLine()) {
                                        responseString = httpin.nextLine();
                                        if (responseString.trim().equalsIgnoreCase("true") || responseString.trim().equalsIgnoreCase("false")) {
                                            response = Boolean.valueOf(responseString);
                                        } else {
                                            System.out.println("The response was not the expected boolean value.");
                                            response = null;
                                        }
                                        if (response == true) {           //true - user's data was updated successfully
                                            System.out.println("Response is " + response);
                                            Toast.makeText(ManageAccountActivity.this, "Your information was updated successfully!", Toast.LENGTH_LONG).show();
                                        } else if (response == false) {                        //false - error occurred
                                            System.out.println("Response is " + response);
                                            AlertDialog.Builder myAlert2 = new AlertDialog.Builder(ManageAccountActivity.this);
                                            myAlert2.setMessage("Oops, an error occurred! Please try again.").create();
                                            myAlert2.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            myAlert2.show();
                                        } else {
                                            System.out.println("The response was not a boolean value.");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {//disconnect after making the connection and executing the query
                                    conn.disconnect();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(ManageAccountActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_jumptofiles) {
            Intent intent = new Intent(this, FilesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_jumptogroups) {
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_account, menu);
        return true;
    }
}
