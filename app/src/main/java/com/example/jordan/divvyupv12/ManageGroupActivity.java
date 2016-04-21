package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.Settings;
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

import javax.microedition.khronos.opengles.GL;

public class ManageGroupActivity extends AppCompatActivity {
    public static Button add_group_member_button, remove_group_member_button, leave_group_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group);

        add_group_member_button = (Button) findViewById(R.id.add_group_member_button);
        add_group_member_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //allow both the group owner and the current members to add new members to the group (NOT restricted to just the owner)

                        JSONArray arr = new JSONArray();
                        String parameterID = "";

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
                                    String json = "";
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
                            try {
                                EditText addmemberField = (EditText) findViewById(R.id.add_group_member_field);
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);
                                    if (addmemberField.getText().toString().equals(obj.getString("username"))) {
                                        System.out.println("Match found!");
                                        parameterID = obj.getString("id");
                                        i = arr.length(); //to exit the for loop
                                    }
                                }

                                //if there is no match at the end of the for loop, say so
                                if (parameterID == "") {
                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
                                    myAlert.setMessage("No match found for the username entered. \n\nPlease check your spelling and try again!").create();
                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    myAlert.show();
                                } else {
                                    try { //must surround with try/catch to filter errors
                                        Uri.Builder builder = new Uri.Builder()
                                                .appendQueryParameter("userid", parameterID) //user id of the person to be added
                                                .appendQueryParameter("groupid", Globals.getInstance().groupIDs[Globals.getInstance().positionOfGroup])
                                                .appendQueryParameter("db", "codedb");
                                        String query = builder.build().getEncodedQuery();
                                        System.out.println("The query: " + query);      //to verify query string

                                        URL url = new URL("http://cslinux.samford.edu/codedb/addmember.php?" + query);
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        System.out.println("The complete url: " + url); //to verify full url
                                        String responseString;
                                        Boolean response;
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
                                                if (response == true) {           //true - user was added to the group, display message and continue
                                                    System.out.println("Response is " + response);
                                                    Toast.makeText(ManageGroupActivity.this, "You have successfully added " + addmemberField.getText().toString() + " to the group.", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(ManageGroupActivity.this, GroupsActivity.class));

                                                } else if (response == false) {                        //false - error occurred or username already exists, do not continue
                                                    System.out.println("Response is " + response);
                                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
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
                                            conn.disconnect();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        remove_group_member_button = (Button) findViewById(R.id.remove_group_member_button);
        remove_group_member_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //only allow deleting from a group as the owner (members cannot delete anyone else)
                        if (Globals.getInstance().groupownerID.equals(Globals.getInstance().userID)) {
                            JSONArray arr = new JSONArray();
                            String parameterID = "";

                            //STEP 1: detect the user that has been typed
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
                                        String json = "";
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
                                try {
                                    EditText removememberField = (EditText) findViewById(R.id.remove_group_member_field);
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject obj = arr.getJSONObject(i);
                                        if (removememberField.getText().toString().equals(obj.getString("username"))) {
                                            System.out.println("Match found!");
                                            parameterID = obj.getString("id");
                                            i = arr.length(); //to exit the for loop
                                        }
                                    }

                                    //if there is no match at the end of the for loop, say so
                                    if (parameterID.equals("")) {
                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
                                        myAlert.setMessage("No match found for the username entered. \n\nPlease check your spelling and try again!").create();
                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        myAlert.show();
                                    } else { //else we found a parameterID which means the inputted username exists
                                        JSONArray arrMem = null;
                                        String[] groupsIDs = null;
                                        String[] uniqueMemberRowID = null;


                                        //STEP 2: find the groupIDs and uniquememberID database row that are associated with the groups that the specified user is a member of
                                        try { //must surround with try/catch to filter errors
                                            Uri.Builder builder = new Uri.Builder()
                                                    .appendQueryParameter("userid", parameterID)
                                                    .appendQueryParameter("db", "codedb");
                                            String query = builder.build().getEncodedQuery();
                                            //System.out.println("The query: " + query);      //to verify query string

                                            String json = "";
                                            URL url = new URL("http://cslinux.samford.edu/codedb/getgrouplist.php?" + query);
                                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                            System.out.println("The complete url: " + url); //to verify full url
                                            try {//to get the response from server
                                                InputStream in = new BufferedInputStream(conn.getInputStream());
                                                Scanner httpin = new Scanner(in);
                                                for (int i = 0; httpin.hasNextLine(); i++) {
                                                    json += httpin.nextLine();
                                                }
                                                arr = new JSONArray(json);
                                                arrMem = (JSONArray) arr.get(1);
                                                System.out.println("The full JSON array contents: " + arr.toString());
                                                System.out.println("The member JSON array contents: " + arrMem.toString());
                                                if (arr.length() == 0) { //if the json array is empty, then this user does not have any owned groups
                                                    Thread.sleep(3000);
                                                    Toast.makeText(ManageGroupActivity.this, "The user you entered is not a member of the current group.", Toast.LENGTH_LONG).show();
                                                } else {                //groups exists, so further checks are needed
                                                    JSONObject obj;
                                                    groupsIDs = new String[arrMem.length()];
                                                    uniqueMemberRowID = new String[arrMem.length()];
                                                    int count = 0;
                                                    for (int i = 0; i < arrMem.length(); i++) {
                                                        obj = arrMem.getJSONObject(i);
                                                        System.out.println("(User) The unique group ID is: " + obj.getString("group_id"));
                                                        groupsIDs[count] = obj.getString("group_id");
                                                        uniqueMemberRowID[count] = obj.getString("id");
                                                        count++;
                                                    }

                                                    //STEP 3: loop through the groups he/she is a member of and determine if it can be deleted
                                                    int pointer = 0;
                                                    Boolean matchFound = false;
                                                    for (int i = 0; i < groupsIDs.length; i++) {
                                                        if (Globals.getInstance().groupIDs[Globals.getInstance().positionOfGroup].equals(groupsIDs[i])) {
                                                            pointer = i;
                                                            matchFound = true;
                                                            break;
                                                        }
                                                    }


                                                    //STEP 4: remove the user if a match was found
                                                    if (matchFound == true) {
                                                        try { //must surround with try/catch to filter errors
                                                            Uri.Builder builder2 = new Uri.Builder()
                                                                    .appendQueryParameter("memberid", uniqueMemberRowID[pointer]) //unique member row, not the userid of the member...
                                                                    .appendQueryParameter("db", "codedb");
                                                            String query2 = builder2.build().getEncodedQuery();
                                                            System.out.println("The query: " + query2);      //to verify query string

                                                            URL url2 = new URL("http://cslinux.samford.edu/codedb/deletemember.php?" + query2);
                                                            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                                                            System.out.println("The complete url: " + url2); //to verify full url
                                                            String responseString;
                                                            Boolean response;
                                                            try {//to get the response from server
                                                                InputStream in2 = new BufferedInputStream(conn2.getInputStream());
                                                                Scanner httpin2 = new Scanner(in2);
                                                                while (httpin2.hasNextLine()) {
                                                                    responseString = httpin2.nextLine();
                                                                    if (responseString.trim().equalsIgnoreCase("true") || responseString.trim().equalsIgnoreCase("false")) {
                                                                        response = Boolean.valueOf(responseString);
                                                                    } else {
                                                                        System.out.println("The response was not the expected boolean value.");
                                                                        response = null;
                                                                    }
                                                                    if (response == true) {           //true - user was added, display message and continue
                                                                        System.out.println("Response is " + response);
                                                                        Toast.makeText(ManageGroupActivity.this, "The user has successfully been removed from the group.", Toast.LENGTH_LONG).show();
                                                                        startActivity(new Intent(ManageGroupActivity.this, GroupsActivity.class));

                                                                    } else if (response == false) {                        //false - error occurred or username already exists, do not continue
                                                                        System.out.println("Response is " + response);
                                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
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
                                                                conn2.disconnect();
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
                                                        myAlert.setMessage("The user you entered is not a member of the current group.").create();
                                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        myAlert.show();
                                                    }

                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                conn.disconnect();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
                            myAlert.setMessage("You cannot remove members from the group because you are not the owner!").create();
                            myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            myAlert.show();
                        }
                    }
                }
        );

        leave_group_button = (Button) findViewById(R.id.leave_group_button);
        leave_group_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //should only allow "leaving" a group, if you are not the owner
                        System.out.println("Globals.getInstance().groupownerID: " + Globals.getInstance().groupownerID);
                        System.out.println("Globals.getInstance().userID: " + Globals.getInstance().userID);
                        System.out.println(Globals.getInstance().groupownerID != Globals.getInstance().userID);
                        System.out.println("Hello??");
                        if (!Globals.getInstance().groupownerID.equals(Globals.getInstance().userID)) {
                            int SDK_INT = android.os.Build.VERSION.SDK_INT;
                            if (SDK_INT > 8) {//allow execution of network connection on the main thread
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                        .permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                try { //must surround with try/catch to filter errors
                                    Uri.Builder builder = new Uri.Builder()
                                            .appendQueryParameter("memberid", Globals.getInstance().uniquememberRowIDs[Globals.getInstance().positionOfGroup]) //unique member row, not the userid of the member...
                                            .appendQueryParameter("db", "codedb");
                                    String query = builder.build().getEncodedQuery();
                                    System.out.println("The query: " + query);      //to verify query string

                                    URL url = new URL("http://cslinux.samford.edu/codedb/deletemember.php?" + query);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    System.out.println("The complete url: " + url); //to verify full url
                                    String responseString;
                                    Boolean response;
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
                                            if (response == true) {           //true - user was added, display message and continue
                                                System.out.println("Response is " + response);
                                                Toast.makeText(ManageGroupActivity.this, "You have successfully left the group.", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(ManageGroupActivity.this, GroupsActivity.class));

                                            } else if (response == false) {                        //false - error occurred or username already exists, do not continue
                                                System.out.println("Response is " + response);
                                                AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
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
                                        conn.disconnect();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else { //the person trying to leave is the owner and should instead use "delete group"
                            AlertDialog.Builder myAlert = new AlertDialog.Builder(ManageGroupActivity.this);
                            myAlert.setMessage("You cannot leave the group because you are the owner! \n\nIf you wish to disband the group, return to the previous" +
                                    " screen and delete the group from there. ").create();
                            myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            myAlert.show();
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
            Toast.makeText(ManageGroupActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_jumptofiles) {
            Intent intent = new Intent(this, FilesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_jumptogroups) {
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_jumptoaccount) {
            Intent intent = new Intent(this, ManageAccountActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_group, menu);
        return true;
    }
}
