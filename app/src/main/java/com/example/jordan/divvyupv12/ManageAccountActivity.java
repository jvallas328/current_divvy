package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    public static Button delete_account_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        OnClickButtonListener();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(ManageAccountActivity.this, "Feature Coming Soon!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
