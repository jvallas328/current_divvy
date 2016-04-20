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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

public class FilesActivity extends AppCompatActivity {
    public static Button save_file_button, delete_file_button;
    String[] items;
    String[] fileIDs;
    String[] filePermissions;
    String[] filecontentsarr;
    String json = "";
    URL url;
    JSONArray arr = new JSONArray();
    JSONArray arrUser = new JSONArray();
    JSONArray arrShared = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {//allow execution of network connection on the main thread
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

                url = new URL("http://cslinux.samford.edu/codedb/getfilelist.php?" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                System.out.println("The complete url: " + url); //to verify full url
                try {//to get the response from server
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    Scanner httpin = new Scanner(in);
                    for (int i = 0; httpin.hasNextLine(); i++) {
                        json += httpin.nextLine();
                    }
                    arr = new JSONArray(json);
                    arrUser = (JSONArray) arr.get(0);
                    arrShared = (JSONArray) arr.get(1);
                    System.out.println("The JSON array contents: " + arr.toString());
                    if (arrUser.length() == 0 && arrShared.length() == 0) { //if the json array is empty, then this user does not have any files
                        Thread.sleep(3000);
                        Toast.makeText(FilesActivity.this, "You do not have any files yet.", Toast.LENGTH_LONG).show();
                    } else {                //files exists
                        JSONObject obj;
                        String[] files = new String[arrUser.length() + arrShared.length()];
                        fileIDs = new String[arrUser.length() + arrShared.length()];
                        filePermissions = new String[arrUser.length() + arrShared.length()];
                        filecontentsarr = new String[arrUser.length() + arrShared.length()];
                        int count = 0;
                        for (int i = 0; i < arrUser.length(); i++) {
                            obj = arrUser.getJSONObject(i);
                            System.out.println("(User) The filename is: " + obj.getString("filename"));
                            files[count] = "Filename:\n" + obj.getString("filename") + "\n\n-Owned\n";
                            filePermissions[count] = "N/A";
                            System.out.println("(User) The file ID is: " + obj.getString("id"));
                            fileIDs[count] = obj.getString("id");
                            count++;
                        }
                        for (int i = 0; i < arrShared.length(); i++) {
                            obj = arrShared.getJSONObject(i);
                            System.out.println("(Shared) The filename is: " + obj.getString("filename"));
                            if (obj.getString("permission").equals("1")) {
                                files[count] = "Filename:\n" + obj.getString("filename") + "\n\n-Shared" + "\n\nPermission \n >Can Edit\n";
                            } else {
                                files[count] = "Filename:\n" + obj.getString("filename") + "\n\n-Shared" + "\n\nPermission \n >Read-Only\n";
                            }
                            System.out.println("(Shared) The file ID is: " + obj.getString("codefile_id"));
                            fileIDs[count] = obj.getString("codefile_id");
                            System.out.println("(Shared) The file permission is: " + obj.getString("permission"));
                            filePermissions[count] = obj.getString("permission");
                            count++;
                        }
                        ArrayAdapter<String> filesAdapter = new ArrayAdapter<String>(this, R.layout.item_view, android.R.id.text1, files);
                        ListView listView = (ListView) findViewById(R.id.listView);
                        listView.setAdapter(filesAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapter, View view, int position, final long id) {
                                try { //must surround with try/catch to filter errors
                                    EditText contentBox = (EditText) findViewById(R.id.fileContentsBox);
                                    if (filePermissions[(int) id].equals("0")) {
                                        contentBox.setClickable(false);
                                        contentBox.setCursorVisible(false);
                                        contentBox.setFocusable(false);
                                        contentBox.setFocusableInTouchMode(false);
                                    } else {
                                        contentBox.setClickable(true);
                                        contentBox.setCursorVisible(true);
                                        contentBox.setFocusable(true);
                                        contentBox.setFocusableInTouchMode(true);
                                    }
                                    String contents = "";
                                    Uri.Builder builder = new Uri.Builder()
                                            .appendQueryParameter("fileid", fileIDs[(int) id])
                                            .appendQueryParameter("db", "codedb");
                                    String query = builder.build().getEncodedQuery();
                                    System.out.println("The query: " + query);      //to verify query string

                                    url = new URL("http://cslinux.samford.edu/codedb/loadfile.php?" + query);
                                    HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();//MAKE GLOBAL LATER
                                    System.out.println("The complete url: " + url); //to verify full url
                                    try {//to get the response from server
                                        InputStream in = new BufferedInputStream(conn2.getInputStream());
                                        Scanner httpin = new Scanner(in);
                                        for (int i = 0; httpin.hasNextLine(); i++) {
                                            if (i != 0) {
                                                contents += "\n" + httpin.nextLine();
                                            } else {
                                                contents += httpin.nextLine();
                                            }
                                        }
                                        final String finalContents = contents;
                                        //arr = new JSONArray(json);
                                        System.out.println("The contents: " + contents);
                                        //now handle loading the files when selected... if a file is empty, display a toast that there is no contents
                                        if (contents.equals("")) {
                                            Toast.makeText(FilesActivity.this, "This file does not have any content.", Toast.LENGTH_LONG).show();
                                        }
                                        EditText text = (EditText) findViewById(R.id.fileContentsBox);
                                        text.setText(contents);

                                        //handle the file being deleted
                                        delete_file_button = (Button) findViewById(R.id.delete_file);
                                        delete_file_button.setOnClickListener(
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //do not let a user delete a file that has been shared with them (they do not own it!)
                                                        //but only do this check if the user has shared files in their list to begin with
                                                        Boolean goodDelete = true; //if true, the delete is good and should be processed, else it is a shared file and should not be processed
                                                        if(arrShared.length() != 0) {
                                                            JSONObject obj2;
                                                            int shared_codefile_ids;
                                                            try {
                                                                for (int i = 0; i < arrShared.length(); i++) {
                                                                    obj2 = arrShared.getJSONObject(i);
                                                                    shared_codefile_ids = Integer.parseInt(obj2.getString("codefile_id"));
                                                                    if(shared_codefile_ids == Integer.parseInt(fileIDs[(int) id])){
                                                                        goodDelete = false;
                                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(FilesActivity.this);
                                                                        myAlert.setMessage("You cannot delete a file that you do not own!").create();
                                                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                        myAlert.show();
                                                                    }
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        if(goodDelete == true){
                                                            //alert a confirmation message
                                                            AlertDialog.Builder myAlert1 = new AlertDialog.Builder(FilesActivity.this);
                                                            myAlert1.setMessage("Are you sure you want to delete this file? \n\nThis will permanently delete your file and " +
                                                                    "other users you shared the file with will no longer be able to access it.").create();
                                                            myAlert1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    try { //must surround with try/catch to filter errors
                                                                        Uri.Builder builder = new Uri.Builder()
                                                                                .appendQueryParameter("fileid", fileIDs[(int) id])
                                                                                .appendQueryParameter("db", "codedb");
                                                                        String query = builder.build().getEncodedQuery();
                                                                        System.out.println("The query: " + query);      //to verify query string

                                                                        URL url = new URL("http://cslinux.samford.edu/codedb/deletefile.php?" + query);
                                                                        HttpURLConnection conn3 = (HttpURLConnection) url.openConnection();
                                                                        System.out.println("The complete url: " + url); //to verify full url
                                                                        try {//to get the response from server
                                                                            InputStream in = new BufferedInputStream(conn3.getInputStream());
                                                                            Scanner httpin = new Scanner(in);
                                                                            String responseString;
                                                                            Boolean response;
                                                                            while (httpin.hasNextLine()) {
                                                                                responseString = httpin.nextLine();
                                                                                if (responseString.trim().equalsIgnoreCase("true") || responseString.trim().equalsIgnoreCase("false")) {
                                                                                    response = Boolean.valueOf(responseString);
                                                                                } else {
                                                                                    System.out.println("The response was not the expected boolean value.");
                                                                                    response = null;
                                                                                }
                                                                                if (response == true) {           //true - file was deleted
                                                                                    System.out.println("Response is " + response);
                                                                                    Toast.makeText(FilesActivity.this, "The file was deleted successfully.", Toast.LENGTH_LONG).show();
                                                                                    finish();                   //finish and reload the activity to get rid of the file in the list
                                                                                    startActivity(getIntent());
                                                                                } else if (response == false) {                        //false - error occurred/did not work
                                                                                    System.out.println("Response is " + response);
                                                                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(FilesActivity.this);
                                                                                    myAlert.setMessage("Oops, an error occurred! Please try again.").create();
                                                                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    });
                                                                                    myAlert.show();
                                                                                } else {
                                                                                    System.out.println("The response was not a boolean value.");
                                                                                }
                                                                            }
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        } finally {//disconnect after making the connection and executing the query
                                                                            conn3.disconnect();
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
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
                                                }
                                        );

                                        save_file_button = (Button) findViewById(R.id.save_file);
                                        save_file_button.setOnClickListener(
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        EditText newtext = (EditText) findViewById(R.id.fileContentsBox);
                                                        try { //must surround with try/catch to filter errors
                                                            //String contents = "";
                                                            Uri.Builder builder = new Uri.Builder()
                                                                    .appendQueryParameter("origtext", finalContents)
                                                                    .appendQueryParameter("newtext", newtext.getText().toString())
                                                                    .appendQueryParameter("userid", Globals.getInstance().userID)
                                                                    .appendQueryParameter("fileid", fileIDs[(int) id])
                                                                    .appendQueryParameter("db", "codedb");
                                                            String query = builder.build().getEncodedQuery();
                                                            System.out.println("The query: " + query);      //to verify query string

                                                            url = new URL("http://cslinux.samford.edu/codedb/patchmake.php");
                                                            HttpURLConnection conn4 = (HttpURLConnection) url.openConnection();//MAKE GLOBAL LATER
                                                            conn4.setRequestMethod("POST");
                                                            conn4.setDoOutput(true);
                                                            conn4.setDoInput(true);
                                                            OutputStream os = conn4.getOutputStream();
                                                            BufferedWriter writer = new BufferedWriter(
                                                                    new OutputStreamWriter(os, "UTF-8"));
                                                            writer.write(query);
                                                            writer.flush();
                                                            writer.close();
                                                            os.close();

                                                            System.out.println("The complete POST url: " + url); //to verify full url
                                                            try {//to get the response from server
                                                                String postContents = "";
                                                                BufferedReader rd = new BufferedReader(new InputStreamReader(conn4.getInputStream()));
                                                                StringBuilder sb = new StringBuilder();
                                                                String line;
                                                                while ((line = rd.readLine()) != null) {
                                                                    postContents += line;
                                                                    System.out.println("entered for loop");
                                                                }
                                                                System.out.println("line " + line);
                                                                System.out.println("The POST contents: " + postContents);
                                                                if (postContents.trim().equals("false")) {
                                                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(FilesActivity.this);
                                                                    myAlert.setMessage("Error. The file was not saved successfully." +
                                                                            "\n\nPlease try again.").create();
                                                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                    myAlert.show();
                                                                } else {
                                                                    Toast.makeText(FilesActivity.this, "The file was saved successfully!", Toast.LENGTH_LONG).show();
                                                                }
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            } finally {//disconnect after making the connection and executing the query
                                                                conn4.disconnect();
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                        );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {//disconnect after making the connection and executing the query
                                        conn2.disconnect();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        });

                    }//password for db is
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {//disconnect after making the connection and executing the query
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        findViewById(R.id.create_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FilesActivity.this, CreateFileActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(FilesActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToFiles(View view) {
        Intent intent = new Intent(this, AddUserToFileActivity.class);
        startActivity(intent);
    }
}
