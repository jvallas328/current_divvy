package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
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
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FilesActivity extends AppCompatActivity {
    public static Button save_file_button, delete_file_button;
    String contentForRealTime = "";
    String[] items;
    String[] fileIDs;
    String[] filePermissions;
    String[] filecontentsarr;
    String json = "";
    URL url;
    JSONArray arr = new JSONArray();
    JSONArray arrUser = new JSONArray();
    JSONArray arrShared = new JSONArray();
    //ScheduledExecutorService executor;

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
                                Globals.getInstance().currentCodeFilePosition = position;
                                Globals.getInstance().currentActivity = FilesActivity.this;

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

                                        System.out.println("The contents: " + contents);
                                        //now handle loading the files when selected... if a file is empty, display a toast that there is no contents
                                        if (contents.equals("")) {
                                            Toast.makeText(FilesActivity.this, "This file does not have any content.", Toast.LENGTH_LONG).show();
                                        }
                                        EditText text = (EditText) findViewById(R.id.fileContentsBox);
                                        text.setText(contents);
                                        Globals.getInstance().oldContent = contents; //for the very first initial load of file, this is my "Old Contents"
                                        Toast.makeText(FilesActivity.this, "Files are Saved Automatically.", Toast.LENGTH_LONG).show();

                                        //handle the file being deleted
                                        delete_file_button = (Button) findViewById(R.id.delete_file);
                                        delete_file_button.setOnClickListener(
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //do not let a user delete a file that has been shared with them (they do not own it!)
                                                        //but only do this check if the user has shared files in their list to begin with
                                                        Boolean goodDelete = true; //if true, the delete is good and should be processed, else it is a shared file and should not be processed
                                                        if (arrShared.length() != 0) {
                                                            JSONObject obj2;
                                                            int shared_codefile_ids;
                                                            try {
                                                                for (int i = 0; i < arrShared.length(); i++) {
                                                                    obj2 = arrShared.getJSONObject(i);
                                                                    shared_codefile_ids = Integer.parseInt(obj2.getString("codefile_id"));
                                                                    if (shared_codefile_ids == Integer.parseInt(fileIDs[(int) id])) {
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

                                                        if (goodDelete == true) {
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
                                                                                    Globals.getInstance().currentActivity = null;
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

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {//disconnect after making the connection and executing the query
                                        conn2.disconnect();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                //REAL TIME PART
                                //make consecutive calls to "loadfile.php" to get any changes that may have taken place on the file
                                //the very first time, we need to set up the oldContent to have the current content
                                final int positionCheck = Globals.getInstance().currentCodeFilePosition;
                                final com.example.jordan.divvyupv12.FilesActivity activityCheck = Globals.getInstance().currentActivity;
                                final Timer myTimer = new Timer();
                                myTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (positionCheck != Globals.getInstance().currentCodeFilePosition || activityCheck != Globals.getInstance().currentActivity) { //user switched to a different file
                                            myTimer.cancel();
                                            System.out.println("~~~~~ Cancelled Timer ~~~~~");
                                        }
                                        TimerMethodSave(id);
                                        TimerMethodLoad(id);
                                        System.out.println("~~~~~ Query Executed ~~~~~");
                                    }
                                }, 0, 5000);


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
                Globals.getInstance().currentActivity = null;
                startActivity(new Intent(FilesActivity.this, CreateFileActivity.class));
            }
        });

        findViewById(R.id.share_file_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder myAlert = new AlertDialog.Builder(FilesActivity.this);
                myAlert.setMessage("How would you like to share a file?").create();
                myAlert.setPositiveButton("With a Group!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Globals.getInstance().currentActivity = null;
                        startActivity(new Intent(FilesActivity.this, ShareFileActivity.class));
                        dialog.dismiss();
                    }
                });
                myAlert.setNegativeButton("With a User!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Globals.getInstance().currentActivity = null;
                        startActivity(new Intent(FilesActivity.this, AddUserToFileActivity.class));
                        dialog.dismiss();
                    }
                });
                myAlert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                myAlert.show();
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
            Globals.getInstance().currentActivity = null;
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(FilesActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_jumptogroups) {
            Globals.getInstance().currentActivity = null;
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_jumptoaccount) {
            Globals.getInstance().currentActivity = null;
            Intent intent = new Intent(this, ManageAccountActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void TimerMethodSave(long id) {

        EditText edittext = (EditText) findViewById(R.id.fileContentsBox);
        String newtext = edittext.getText().toString();
        System.out.println("The old text: " + Globals.getInstance().oldContent);
        System.out.println("The new text: " + newtext);
        System.out.println("The userID is: " + Globals.getInstance().userID);
        System.out.println("The fileID is: " + fileIDs[(int) id]);
        try {
            //System.out.println("~~~~~ Making my Query now! ~~~~~");
            HttpURLConnection conn4 = null;
            try { //must surround with try/catch to filter errors
                //String contents = "";
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("origtext", Globals.getInstance().oldContent)
                        .appendQueryParameter("newtext", newtext)
                        .appendQueryParameter("userid", Globals.getInstance().userID)
                        .appendQueryParameter("fileid", fileIDs[(int) id])
                        .appendQueryParameter("db", "codedb");
                String query = builder.build().getEncodedQuery();
                //System.out.println("The query: " + query);      //to verify query string

                url = new URL("http://cslinux.samford.edu/codedb/patchmake.php");
                conn4 = (HttpURLConnection) url.openConnection();//MAKE GLOBAL LATER
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

                try {//to get the response from server
                    String postContents = "";
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn4.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        postContents += line;
                    }
                    System.out.println("The query: " + query);
                    System.out.println("The POST contents: " + postContents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {//disconnect after making the connection and executing the query
                conn4.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Globals.getInstance().oldContent = newtext; //old content now needs to be set to new content
        Globals.getInstance().cursorPosition = edittext.getSelectionStart();
        this.runOnUiThread(Timer_Tick2);
    }

    public void TimerMethodLoad(long id) {
        Globals.getInstance().changeIndicator = false;  //for detecting changes by other users
        EditText edittext = (EditText) findViewById(R.id.fileContentsBox);
        try {
            System.out.println("Checking for changes made by other users...");
            System.out.println("The fileID is: " + fileIDs[(int) id]);
            String contents = "";
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("fileid", fileIDs[(int) id])
                    .appendQueryParameter("db", "codedb");
            String query = builder.build().getEncodedQuery();

            url = new URL("http://cslinux.samford.edu/codedb/loadfile.php?" + query);
            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
            System.out.println("The complete url is : " + url); //to verify full url
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
                System.out.println("The contents before request: " + Globals.getInstance().oldContent);
                System.out.println("The contents after request: " + contents);

                //if a change is detected, indicate this and update the old contents
                if (!Globals.getInstance().oldContent.equals(contents)) {
                    Globals.getInstance().changeIndicator = true;
                    Globals.getInstance().oldContent = contents;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Globals.getInstance().cursorPosition = edittext.getSelectionStart();
        this.runOnUiThread(Timer_Tick1);
    }

    private Runnable Timer_Tick1 = new Runnable() {
        public void run() {
            if (Globals.getInstance().changeIndicator == true) { //update the editText box and put cursor back in the closest position
                EditText edittext = (EditText) findViewById(R.id.fileContentsBox);
                edittext.setText(Globals.getInstance().oldContent); //because its actually the new content that has just been changed
                System.out.println("The cursor position i am trying to place: " + Globals.getInstance().cursorPosition);
                edittext.setSelection(Globals.getInstance().cursorPosition);
                Toast.makeText(FilesActivity.this, "An edit has been made", Toast.LENGTH_SHORT).show();
            }
            //This method runs in the same thread as the UI.
            //Do something to the UI thread here

        }
    };

    private Runnable Timer_Tick2 = new Runnable() {
        public void run() {
            EditText edittext = (EditText) findViewById(R.id.fileContentsBox);
            edittext.setText(Globals.getInstance().oldContent); //because its actually the new content that has just been changed
            System.out.println("The cursor position i am trying to place: " + Globals.getInstance().cursorPosition);
            edittext.setSelection(Globals.getInstance().cursorPosition);
            //This method runs in the same thread as the UI.
            //Do something to the UI thread here
        }
    };

}
