package com.example.jordan.divvyupv12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ShareFileActivity extends AppCompatActivity {
    public static Button add_group_to_file_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);

        //disable the button until a valid group is selected
        final Button buttonRename = (Button) findViewById(R.id.add_group_to_file_actual);
        buttonRename.setEnabled(false);


        String json = "";
        URL url;
        JSONArray arr = new JSONArray();
        JSONArray arrMem = new JSONArray();
        JSONArray arrOwn = new JSONArray();
        final String[] groupsIDs;
        final String[] groupOwnerIDs;
        final String[] uniqueMemberRowID;
        Spinner dropdown = (Spinner) findViewById(R.id.spinner2);

        //set up a spinner selection for the group
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
                    if (arrOwn.length() == 0 && arrMem.length() == 0) { //if the json array is empty, then this user does not have any groups and button should be disabled
                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                        myAlert.setMessage("You do not have any groups! Please create a group first.").create();
                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        myAlert.show();

                        ArrayList arrayList = new ArrayList();
                        arrayList.add(0, "You have no groups!");
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_view, android.R.id.text1, arrayList) {

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.BLACK);
                                return view;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.WHITE);
                                return view;
                            }
                        };
                        dropdown.setAdapter(adapter);

                    } else {                //groups exists, so continue checking
                        JSONObject obj;
                        String[] groups = new String[arrOwn.length() + arrMem.length()];
                        groupOwnerIDs = new String[arrOwn.length() + arrMem.length()];
                        groupsIDs = new String[arrOwn.length() + arrMem.length()];
                        uniqueMemberRowID = new String[arrOwn.length() + arrMem.length()];
                        int count = 0;
                        for (int i = 0; i < arrOwn.length(); i++) {
                            obj = arrOwn.getJSONObject(i);
                            System.out.println("(User) The groupname is: " + obj.getString("name"));
                            groups[count] = obj.getString("name") + " (Owner)";
                            System.out.println("(User) The unique group ID is: " + obj.getString("id"));
                            groupsIDs[count] = obj.getString("id");
                            groupOwnerIDs[count] = obj.getString("user_id");
                            count++;
                        }
                        for (int i = 0; i < arrMem.length(); i++) {
                            obj = arrMem.getJSONObject(i);
                            System.out.println("(Shared) The groupname is: " + obj.getString("name"));
                            groups[count] = obj.getString("name") + " (Member)";
                            System.out.println("(User) The unique group ID is: " + obj.getString("group_id"));
                            groupsIDs[count] = obj.getString("group_id");
                            groupOwnerIDs[count] = obj.getString("owner_id");
                            uniqueMemberRowID[count] = obj.getString("id");
                            count++;
                        }

                        ArrayList arrayList = new ArrayList();
                        for (int i = 0; i < groups.length; i++) {
                            arrayList.add(groups[i]);
                        }
                        arrayList.add(0, "Select a Group...");
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_view, android.R.id.text1, arrayList) {

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.BLACK);
                                return view;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.WHITE);
                                return view;
                            }
                        };
                        dropdown.setAdapter(adapter);

                        //store the data when one is selected.
                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try { //must surround with try/catch to filter errors
                                    if (id == 0) {
                                        //do nothing (because otherwise throws an error since "select group" is technically a selection)
                                    } else {
                                        String json1 = "";
                                        JSONArray arr1;
                                        Uri.Builder builder = new Uri.Builder()
                                                .appendQueryParameter("groupid", groupsIDs[(int) id - 1])
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
                                                buttonRename.setEnabled(false);
                                                //only the current user exists in the group.... so dont let them click the button
                                                AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                                                myAlert.setMessage("You are the only person in this group! Please select a group with other members.").create();
                                                myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                myAlert.show();

                                            } else { //members exist in the group
                                                buttonRename.setEnabled(true);


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
                                                groupmemberIDs[groupmemberIDs.length - 1] = groupOwnerIDs[position - 1];

                                                System.out.println("Here is the list of group members that i will be sharing with: ");
                                                Globals.getInstance().groupmemberIDsPass = new String[groupmemberIDs.length];
                                                for (int i = 0; i < groupmemberIDs.length; i++) {
                                                    Globals.getInstance().groupmemberIDsPass[i] = groupmemberIDs[i];
                                                    System.out.println(groupmemberIDs[i]);
                                                }


                                                System.out.println("position is: " + (position - 1));
                                                System.out.println("id is: " + (id - 1));
                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {//disconnect after making the connection and executing the query
                                            conn1.disconnect();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                return;
                            }
                        });

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


        add_group_to_file_button = (Button) findViewById(R.id.add_group_to_file_actual);
        add_group_to_file_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText fileNameField = (EditText) findViewById(R.id.File_Name_Field);
                        RadioButton editRadioButton = (RadioButton) findViewById(R.id.Can_Edit_Select_Group);
                        RadioButton readonlyRadioButton = (RadioButton) findViewById(R.id.Read_Only_Select_Group);

                        try { //must surround with try/catch to filter errors
                            JSONArray arrFull;
                            JSONArray arrUser;
                            JSONArray arrShared;
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
                                for (int i = 0; httpin.hasNextLine(); i++) {
                                    json3 += httpin.nextLine();
                                }
                                arrFull = new JSONArray(json3);
                                arrUser = (JSONArray) arrFull.get(0);
                                arrShared = (JSONArray) arrFull.get(1);
                                System.out.println("The JSON array contents: " + arrFull.toString());
                                if (arrUser.length() == 0) { //if the json array is empty, then this user does not have any files
                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                                    myAlert.setMessage("You do not own any files yet! \n\nCreate a new file and try again! \n\n(Note: Currently, we only allow a file to be shared by its owner.)").create();
                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    myAlert.show();
                                } else {                //user exists
                                    boolean sharedFile = true;
                                    JSONObject obj;
                                    System.out.println("The text field: " + fileNameField.getText().toString());
                                    for (int i = 0; i < arrUser.length(); i++) { //check to see if the file exists and is owned by the current user
                                        obj = arrUser.getJSONObject(i);
                                        System.out.println("A potential match file owned by the current user: " + obj.getString("filename"));
                                        if (fileNameField.getText().toString().equals(obj.getString("filename"))) {
                                            System.out.println("Match found!");
                                            parameterFileID = obj.getString("id");
                                            i = arrUser.length();
                                            sharedFile = false;
                                        }
                                    }
                                    for (int i = 0; i < arrShared.length(); i++) { //check to see if the file was a shared file and the current user is not the owner
                                        obj = arrShared.getJSONObject(i);
                                        System.out.println("A potential match file owned by the current user: " + obj.getString("filename"));
                                        if (fileNameField.getText().toString().equals(obj.getString("filename"))) {
                                            System.out.println("Match found!");
                                            parameterFileID = obj.getString("codefile_id");
                                            i = arrShared.length();
                                            sharedFile = true;
                                        }
                                    }
                                    if (parameterFileID == "") { //if the parameterFileID is still null after all of this, then no match was found
                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                                        myAlert.setMessage("No match found for the filename entered. \n\nPlease check your spelling and try again!").create();
                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        myAlert.show();
                                    } else if (sharedFile == true) { //if sharedFile is true, then the file was found but not owned by the user
                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                                        myAlert.setMessage("You cannot share this file because you are not the owner. \n\nPlease try again, but this time with a file you own.").create();
                                        myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        myAlert.show();
                                    } else {    //file  was found and owned by the user
                                        String permissionValue = "";
                                        if (editRadioButton.isChecked()) { //permission should be a 1
                                            permissionValue = "1";
                                        } else {
                                            permissionValue = "0";
                                        }
                                        try { //must surround with try/catch to filter errors
                                            boolean moveOn = true;
                                            for (int i = 0; i < Globals.getInstance().groupmemberIDsPass.length; i++) {
                                                if (!Globals.getInstance().userID.equals(Globals.getInstance().groupmemberIDsPass[i])) {
                                                    String json2 = "";
                                                    Uri.Builder builder2 = new Uri.Builder()
                                                            .appendQueryParameter("userid", Globals.getInstance().groupmemberIDsPass[i])
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
                                                        System.out.println("The returned contents of the file: " + json2);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    } finally {//disconnect after making the connection and executing the query
                                                        conn3.disconnect();
                                                    }
                                                    //go to hub page if the login was successful
                                                    if (json2.equals("false")) {
                                                        moveOn = false;
                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(ShareFileActivity.this);
                                                        myAlert.setMessage("Oops, something went wrong! One or more of the users in the group were not added to the file.\n\nPlease try again.").create();
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
                                            if (moveOn == true) {
                                                Toast.makeText(ShareFileActivity.this, "The file was shared with the group successfully!", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent("com.example.jordan.divvyupv12.FilesActivity");
                                                startActivity(intent);
                                            } else {
                                                return;
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }//password for db is
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {//disconnect after making the connection and executing the query
                                conn2.disconnect();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_file, menu);
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
            Toast.makeText(ShareFileActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
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
}
