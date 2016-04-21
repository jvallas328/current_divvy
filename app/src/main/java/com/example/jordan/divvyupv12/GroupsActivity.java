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
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GroupsActivity extends AppCompatActivity {
    public static Button delete_group_button, manage_group_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        String json = "";
        URL url;
        JSONArray arr = new JSONArray();
        JSONArray arrMem = new JSONArray();
        JSONArray arrOwn = new JSONArray();
        final String[] groupsIDs;
        final String[] groupOwnerIDs;
        final String[] uniqueMemberRowID; //used to delete a member from a group

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

                url = new URL("http://cslinux.samford.edu/codedb/getgrouplist.php?" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                System.out.println("The complete url: " + url); //to verify full url
                try {//to get the response from server
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    Scanner httpin = new Scanner(in);
                    for (int i = 0; httpin.hasNextLine(); i++) {
                        json += httpin.nextLine();
                    }
                    arr = new JSONArray(json);
                    arrOwn = (JSONArray) arr.get(0);
                    arrMem = (JSONArray) arr.get(1);
                    System.out.println("The full JSON array contents: " + arr.toString());
                    System.out.println("The owner JSON array contents: " + arrOwn.toString());
                    System.out.println("The member JSON array contents: " + arrMem.toString());
                    if (arr.length() == 0) { //if the json array is empty, then this user does not have any owned groups
                        Thread.sleep(3000);
                        Toast.makeText(GroupsActivity.this, "You do not have any groups yet.", Toast.LENGTH_LONG).show();
                    } else {                //groups exists
                        JSONObject obj;
                        String[] groups = new String[arrOwn.length() + arrMem.length()];
                        groupOwnerIDs = new String[arrOwn.length() + arrMem.length()];
                        groupsIDs = new String[arrOwn.length() + arrMem.length()];
                        uniqueMemberRowID = new String[arrOwn.length() + arrMem.length()];
                        int count = 0;
                        for (int i = 0; i < arrOwn.length(); i++) {
                            obj = arrOwn.getJSONObject(i);
                            System.out.println("(User) The groupname is: " + obj.getString("name"));
                            groups[count] = "Name:\n" + obj.getString("name") + "\n\nStatus: \n-Owner\n";
                            System.out.println("(User) The unique group ID is: " + obj.getString("id"));
                            groupsIDs[count] = obj.getString("id");
                            groupOwnerIDs[count] = obj.getString("user_id");
                            count++;
                        }
                        for (int i = 0; i < arrMem.length(); i++) {
                            obj = arrMem.getJSONObject(i);
                            System.out.println("(Shared) The groupname is: " + obj.getString("name"));
                            groups[count] = "Name:\n" + obj.getString("name") + "\n\nStatus: \n-Member\n";
                            System.out.println("(User) The unique group ID is: " + obj.getString("group_id"));
                            groupsIDs[count] = obj.getString("group_id");
                            groupOwnerIDs[count] = obj.getString("owner_id");
                            uniqueMemberRowID[count] = obj.getString("id");
                            count++;
                        }

                        ArrayAdapter<String> groupsAdapter = new ArrayAdapter<String>(this, R.layout.item_view, android.R.id.text1, groups);
                        ListView listView = (ListView) findViewById(R.id.group_list);
                        listView.setAdapter(groupsAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapter, View view, final int position, final long id) {
                                try { //must surround with try/catch to filter errors

                                    try { //must surround with try/catch to filter errors
                                        String json1 = "";
                                        JSONArray arr1;
                                        Uri.Builder builder = new Uri.Builder()
                                                .appendQueryParameter("groupid", groupsIDs[(int) id])
                                                .appendQueryParameter("db", "codedb");
                                        String query = builder.build().getEncodedQuery();
                                        //System.out.println("The query: " + query);      //to verify query string

                                        URL url1 = new URL("http://cslinux.samford.edu/codedb/getmembers.php?" + query);
                                        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
                                        System.out.println("The complete url: " + url1); //to verify full url
                                        try {//to get the response from server
                                            InputStream in = new BufferedInputStream(conn1.getInputStream());
                                            Scanner httpin = new Scanner(in);
                                            for (int i = 0; httpin.hasNextLine(); i++) {
                                                json1 += httpin.nextLine();
                                            }

                                            //if this throws an exception, its because the group has no members and should just show "You"
                                            if (json1.contains("uhoh")) {
                                                //display the current user as the only username in the member list

                                                try { //must surround with try/catch to filter errors
                                                    String json2 = "";
                                                    JSONArray arr2 = null;
                                                    Uri.Builder builder2 = new Uri.Builder()
                                                            .appendQueryParameter("db", "codedb");
                                                    String query2 = builder2.build().getEncodedQuery();
                                                    System.out.println("The query: " + query2);      //to verify query string

                                                    URL url2 = new URL("http://cslinux.samford.edu/codedb/getusers.php?" + query2);
                                                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                                                    System.out.println("The complete url: " + url2); //to verify full url
                                                    try {//to get the response from server
                                                        InputStream in2 = new BufferedInputStream(conn2.getInputStream());
                                                        Scanner httpin2 = new Scanner(in2);
                                                        while (httpin2.hasNextLine()) {
                                                            json2 += httpin2.nextLine();
                                                        }
                                                        arr2 = new JSONArray(json2);
                                                        System.out.println("The JSON array contents: " + arr2.toString());
                                                    } catch (Exception e2) {
                                                        e2.printStackTrace();
                                                    } finally {//disconnect after making the connection and executing the query
                                                        conn2.disconnect();
                                                    }

                                                    //match the user names with the userids
                                                    String[] usernames = new String[1];
                                                    for (int i = 0; i < arr2.length(); i++) {
                                                        JSONObject obj2 = arr2.getJSONObject(i);
                                                        System.out.println("obj2 = " + obj2.toString());
                                                        //System.out.println("Does groupmemberIDs[" + i + "]:" + groupmemberIDs[i] + " == obj2.getString('id'), which is: " + obj2.getString("id"));
                                                        if (Globals.getInstance().userID.equals(obj2.getString("id"))) {
                                                            usernames[i] = obj2.getString("username") + " (Owner)";
                                                            break;
                                                        }
                                                    }


                                                    //display the usernames in the member list
                                                    ArrayAdapter<String> membersAdapter = new ArrayAdapter<String>(GroupsActivity.this, R.layout.item_view, android.R.id.text1, usernames);
                                                    ListView listView2 = (ListView) findViewById(R.id.members_list);
                                                    listView2.setAdapter(membersAdapter);

                                                    //put the group member ids and usernames into a global array for use in the manage group section
                                                    Globals.getInstance().groupmemberIDs = new String[1];
                                                    Globals.getInstance().groupmemberIDs[0] = Globals.getInstance().userID;

                                                    Globals.getInstance().groupmemberUsernames = new String[usernames.length];
                                                    for (int i = 0; i < usernames.length; i++) {
                                                        Globals.getInstance().groupmemberUsernames[i] = usernames[i];
                                                    }

                                                    Globals.getInstance().uniquememberRowIDs = new String[uniqueMemberRowID.length];
                                                    for (int i = 0; i < uniqueMemberRowID.length; i++) {
                                                        Globals.getInstance().uniquememberRowIDs[i] = uniqueMemberRowID[i];
                                                    }

                                                    Globals.getInstance().groupIDs = new String[groupsIDs.length];
                                                    for (int i = 0; i < groupsIDs.length; i++) {
                                                        Globals.getInstance().groupIDs[i] = groupsIDs[i];
                                                    }

                                                    Globals.getInstance().positionOfGroup = position;

                                                    Globals.getInstance().groupownerID = groupOwnerIDs[position];

                                                } catch (Exception e3) {
                                                    e3.printStackTrace();
                                                }

                                            } else { //members exist in the group
                                                arr1 = new JSONArray(json1);
                                                System.out.println("The JSON array contents: " + arr1.toString());

                                                //even if the array is empty, the person accessing the group is the owner, so the actual group is not empty
                                                //+ 1 to include the actual owner in the member list
                                                JSONObject obj1;
                                                String[] groupmemberIDs = new String[arr1.length() + 1];
                                                int count = 0;
                                                for (int i = 0; i < arr1.length(); i++) { //get all of the user ids of the members in the current group
                                                    obj1 = arr1.getJSONObject(i);
                                                    groupmemberIDs[count] = obj1.getString("user_id");
                                                    System.out.println("(User) The user that is a member of this group: " + groupmemberIDs[count]);
                                                    count++;
                                                }

                                                //the ownerID is placed in the last cell of the array
                                                groupmemberIDs[groupmemberIDs.length - 1] = groupOwnerIDs[position];

                                                String ownerID;
                                                //determine the owner
                                                System.out.println("position is: " + position);
                                                System.out.println("id is: " + id);


                                                //now use getusers call to figure out their username and display in members list
                                                try { //must surround with try/catch to filter errors
                                                    String json2 = "";
                                                    JSONArray arr2 = null;
                                                    Uri.Builder builder2 = new Uri.Builder()
                                                            .appendQueryParameter("db", "codedb");
                                                    String query2 = builder2.build().getEncodedQuery();
                                                    System.out.println("The query: " + query2);      //to verify query string

                                                    URL url2 = new URL("http://cslinux.samford.edu/codedb/getusers.php?" + query2);
                                                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                                                    System.out.println("The complete url: " + url2); //to verify full url
                                                    try {//to get the response from server
                                                        InputStream in2 = new BufferedInputStream(conn2.getInputStream());
                                                        Scanner httpin2 = new Scanner(in2);
                                                        while (httpin2.hasNextLine()) {
                                                            json2 += httpin2.nextLine();
                                                        }
                                                        arr2 = new JSONArray(json2);
                                                        System.out.println("The JSON array contents: " + arr2.toString());
                                                    } catch (Exception e2) {
                                                        e2.printStackTrace();
                                                    } finally {//disconnect after making the connection and executing the query
                                                        conn2.disconnect();
                                                    }

                                                    //match the user names with the userids
                                                    String[] usernames = new String[groupmemberIDs.length];
                                                    for (int i = 0; i < groupmemberIDs.length; i++) {
                                                        for (int j = 0; j < arr2.length(); j++) {
                                                            JSONObject obj2 = arr2.getJSONObject(j);
                                                            System.out.println("obj2 = " + obj2.toString());
                                                            //System.out.println("Does groupmemberIDs[" + i + "]:" + groupmemberIDs[i] + " == obj2.getString('id'), which is: " + obj2.getString("id"));
                                                            if (groupmemberIDs[i].equals(obj2.getString("id"))) {
                                                                usernames[i] = obj2.getString("username");
                                                                System.out.println("Does groupmemberIDs[" + i + "]:" + groupmemberIDs[i] + " == groupOwnerIDs[" + position + "]:" + groupOwnerIDs[position]);
                                                                if (groupmemberIDs[i].equals(groupOwnerIDs[position])) { //concatenate (Owner) if its the owner
                                                                    usernames[i] += " (Owner)";
                                                                }
                                                                break;
                                                            }
                                                        }
                                                        System.out.println("Found a match possibly..." + usernames[i]);
                                                    }

                                                    //display the usernames in the member list
                                                    ArrayAdapter<String> membersAdapter = new ArrayAdapter<String>(GroupsActivity.this, R.layout.item_view, android.R.id.text1, usernames);
                                                    ListView listView2 = (ListView) findViewById(R.id.members_list);
                                                    listView2.setAdapter(membersAdapter);

                                                    //put the group member ids and usernames into a global array for use in the manage group section
                                                    Globals.getInstance().groupmemberIDs = new String[groupmemberIDs.length];
                                                    for (int i = 0; i < groupmemberIDs.length; i++) {
                                                        Globals.getInstance().groupmemberIDs[i] = groupmemberIDs[i];
                                                    }

                                                    Globals.getInstance().groupmemberUsernames = new String[usernames.length];
                                                    for (int i = 0; i < usernames.length; i++) {
                                                        Globals.getInstance().groupmemberUsernames[i] = usernames[i];
                                                    }

                                                    Globals.getInstance().uniquememberRowIDs = new String[uniqueMemberRowID.length];
                                                    for (int i = 0; i < uniqueMemberRowID.length; i++) {
                                                        Globals.getInstance().uniquememberRowIDs[i] = uniqueMemberRowID[i];
                                                    }

                                                    Globals.getInstance().groupIDs = new String[groupsIDs.length];
                                                    for (int i = 0; i < groupsIDs.length; i++) {
                                                        Globals.getInstance().groupIDs[i] = groupsIDs[i];
                                                    }

                                                    Globals.getInstance().positionOfGroup = position;
                                                    Globals.getInstance().groupownerID = groupOwnerIDs[position];

                                                } catch (Exception e3) {
                                                    e3.printStackTrace();
                                                }
                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {//disconnect after making the connection and executing the query
                                            conn1.disconnect();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                    //handle the group being deleted
                                    delete_group_button = (Button) findViewById(R.id.delete_group_button);
                                    delete_group_button.setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //do not let a user delete a group that they are not the owner of
                                                    //but only do this check if the user is a member in some group in their list to begin with
                                                    if (Globals.getInstance().userID.equals(groupOwnerIDs[position])) {
                                                        AlertDialog.Builder myAlert1 = new AlertDialog.Builder(GroupsActivity.this);
                                                        myAlert1.setMessage("Are you sure you want to delete this group? \n\nThis will permanently delete your group and " +
                                                                "the current members will no longer be able to access it.").create();
                                                        myAlert1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                try { //must surround with try/catch to filter errors
                                                                    Uri.Builder builder = new Uri.Builder()
                                                                            .appendQueryParameter("groupid", groupsIDs[(int) id])
                                                                            .appendQueryParameter("db", "codedb");
                                                                    String query = builder.build().getEncodedQuery();
                                                                    System.out.println("The query: " + query);      //to verify query string

                                                                    URL url = new URL("http://cslinux.samford.edu/codedb/deletegroup.php?" + query);
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
                                                                                Toast.makeText(GroupsActivity.this, "The group was deleted successfully.", Toast.LENGTH_LONG).show();
                                                                                finish();                   //finish and reload the activity to get rid of the file in the list
                                                                                startActivity(getIntent());
                                                                            } else if (response == false) {                        //false - error occurred/did not work
                                                                                System.out.println("Response is " + response);
                                                                                AlertDialog.Builder myAlert = new AlertDialog.Builder(GroupsActivity.this);
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
                                                        //else i am not the owner and should not be allowed to delete the group
                                                    } else {
                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(GroupsActivity.this);
                                                        myAlert.setMessage("You cannot delete a group that you do not own!").create();
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

                                    //set a listener for manage group button being clicked
                                    manage_group_button = (Button) findViewById(R.id.manage_group_button);
                                    manage_group_button.setOnClickListener(
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent2 = new Intent("com.example.jordan.divvyupv12.ManageGroupActivity");
                                                    startActivity(intent2);
                                                }
                                            }
                                    );

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
        findViewById(R.id.create_group_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsActivity.this, CreateGroupActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groups, menu);
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
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(GroupsActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_jumptofiles) {
            Intent intent = new Intent(this, FilesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_jumptoaccount) {
            Intent intent = new Intent(this, ManageAccountActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
