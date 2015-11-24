package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AddUserToFileActivity extends AppCompatActivity {
    public static Button add_user_to_file_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_file);
        OnClickButtonListener();
    }

    public void OnClickButtonListener() {
        add_user_to_file_button = (Button) findViewById(R.id.add_user_to_file_actual);
        add_user_to_file_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText usernameField = (EditText) findViewById(R.id.Username_Field);
                        EditText filenameField = (EditText) findViewById(R.id.Filename_Field);
                        RadioButton editRadioButton = (RadioButton) findViewById(R.id.Can_Edit_Select);
                        RadioButton readonlyRadioButton = (RadioButton) findViewById(R.id.Read_Only_Select);;
                        String parameterID = "";
                        String json = "";
                        JSONArray arr = new JSONArray();
                        String json2 = "";
                        JSONArray arr2 = new JSONArray();

                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8) {//allow execution of network connection on the main thread
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                    .permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            try { //must surround with try/catch to filter errors
                                Uri.Builder builder = new Uri.Builder()
                                        .appendQueryParameter("db", "codedb");
                                String query = builder.build().getEncodedQuery();
                                System.out.println("The query: " + query);      //to verify query string

                                URL url = new URL("http://cslinux.samford.edu/codedb/getusers.php?" + query);
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {//disconnect after making the connection and executing the query
                                    conn.disconnect();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //get the username
                            System.out.println("The text field: " + usernameField.getText().toString());
                            try {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);
                                    System.out.println("A potential match user: " + obj.getString("username"));
                                    if(usernameField.getText().toString() == obj.getString("username")){
                                        System.out.println("Match found!");
                                        parameterID = obj.getString("id");
                                    }
                                }
                                //if there is no match at the end of the for loop, say so
                                if(parameterID == ""){
                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(AddUserToFileActivity.this);
                                    myAlert.setMessage("No match found for the username entered. \n\nPlease check your spelling and try again!").create();
                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    myAlert.show();
                                }else {
                                    try { //must surround with try/catch to filter errors
                                        JSONArray arrFull;
                                        JSONArray arrUser;
                                        String json3 = "";
                                        String parameterFileID = "";
                                        Uri.Builder builder = new Uri.Builder()
                                                .appendQueryParameter("userid", Globals.getInstance().userID)
                                                .appendQueryParameter("db", "codedb");
                                        String query = builder.build().getEncodedQuery();

                                        URL url = new URL("http://cslinux.samford.edu/codedb/getfilelist.php?" + query);
                                        HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
                                        System.out.println("The complete url: " + url); //to verify full url
                                        try {//to get the response from server
                                            InputStream in = new BufferedInputStream(conn2.getInputStream());
                                            Scanner httpin = new Scanner(in);
                                            for(int i = 0; httpin.hasNextLine(); i++) {
                                                json3 += httpin.nextLine();
                                            }
                                            arrFull = new JSONArray(json3);
                                            arrUser = (JSONArray)arrFull.get(0);
                                            System.out.println("The JSON array contents: " + arrFull.toString());
                                            if(arrUser.length() == 0) { //if the json array is empty, then this user does not have any files
                                                AlertDialog.Builder myAlert = new AlertDialog.Builder(AddUserToFileActivity.this);
                                                myAlert.setMessage("You do not own any files yet! \n\n(Note: Currently, we only allow a file to be shared by its creator.)").create();
                                                myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                myAlert.show();
                                            } else {                //user exists
                                                JSONObject obj;
                                                System.out.println("The text field: " + filenameField.getText().toString());
                                                for (int i = 0; i < arrUser.length(); i++) {
                                                    obj = arrUser.getJSONObject(i);
                                                    System.out.println("A potential match file owned by the current user: " + obj.getString("filename"));
                                                    if(filenameField.getText().toString() == obj.getString("filename")){
                                                        System.out.println("Match found!");
                                                        parameterFileID = obj.getString("id");
                                                    }
                                                }
                                                if(parameterFileID == ""){
                                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(AddUserToFileActivity.this);
                                                    myAlert.setMessage("No match found for the filename entered. \n\nPlease check your spelling and try again!").create();
                                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    myAlert.show();
                                                }else {
                                                    String permissionValue = "";
                                                    if(editRadioButton.isChecked()){ //permission should be a 1
                                                        permissionValue = "1";
                                                    } else {
                                                        permissionValue = "0";
                                                    }
                                                    //call to add user to file
                                                    try { //must surround with try/catch to filter errors
                                                        Uri.Builder builder2 = new Uri.Builder()
                                                                .appendQueryParameter("userid", parameterID)
                                                                .appendQueryParameter("permission", permissionValue)
                                                                .appendQueryParameter("fileid", parameterFileID)
                                                                .appendQueryParameter("db", "codedb");
                                                        String query2 = builder2.build().getEncodedQuery();

                                                        URL url2 = new URL("http://cslinux.samford.edu/codedb/addusertofile.php?" + query2);
                                                        HttpURLConnection conn3 = (HttpURLConnection) url2.openConnection();
                                                        System.out.println("The complete url: " + url2); //to verify full url
                                                        try {//to get the response from server
                                                            InputStream in2 = new BufferedInputStream(conn3.getInputStream());
                                                            Scanner httpin2 = new Scanner(in2);
                                                            while (httpin2.hasNextLine()) {
                                                                json2 += httpin2.nextLine();
                                                            }
                                                            arr2 = new JSONArray(json2);
                                                            System.out.println("The JSON array contents: " + arr2.toString());
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        } finally {//disconnect after making the connection and executing the query
                                                            conn3.disconnect();
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    //go to hub page if the login was successful
                                                    Intent intent = new Intent("com.example.jordan.divvyupv12.FilesActivity");
                                                    startActivity(intent);
                                                }
                                            }//password for db is
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        finally{//disconnect after making the connection and executing the query
                                            conn2.disconnect();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }
}
