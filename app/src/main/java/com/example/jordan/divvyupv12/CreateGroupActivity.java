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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CreateGroupActivity extends AppCompatActivity {
    public static Button create_group_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        findViewById(R.id.create_new_group_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText groupName = (EditText) findViewById(R.id.group_name_field);
                String content = "";

                //require the group name field to not be empty
                if(groupName.getText().toString().equals("")){
                    AlertDialog.Builder myAlert = new AlertDialog.Builder(CreateGroupActivity.this);
                    myAlert.setMessage("Please give the new group a name.").create();
                    myAlert.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    myAlert.show();
                } else {
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8) {//allow execution of network connection on the main thread
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        try { //must surround with try/catch to filter errors
                            Uri.Builder builder = new Uri.Builder()

                                    .appendQueryParameter("userid", Globals.getInstance().userID)
                                    .appendQueryParameter("name", groupName.getText().toString())
                                    .appendQueryParameter("db", "codedb");
                            String query = builder.build().getEncodedQuery();
                            System.out.println("The query: " + query);      //to verify query string
                            URL url = new URL("http://cslinux.samford.edu/codedb/addgroup.php?" + query);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            System.out.println("The complete url: " + url); //to verify full url
                            try {//to get the response from server
                                InputStream in = new BufferedInputStream(conn.getInputStream());
                                Scanner httpin = new Scanner(in);
                                while (httpin.hasNextLine()) {
                                    content += httpin.nextLine();
                                }
                                System.out.println("The content is " + content);
                                if (content.trim().equals("false")) {
                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(CreateGroupActivity.this);
                                    myAlert.setMessage("Error. The group creation was unsuccessful." +
                                            "\n\nPlease try again.").create();
                                    myAlert.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    myAlert.show();
                                } else {
                                    Toast.makeText(CreateGroupActivity.this, "The group was created successfully!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(CreateGroupActivity.this, GroupsActivity.class));
                                }
                                //System.out.println("The id of the created file is " + httpin.toString());
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
        });
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
            Toast.makeText(CreateGroupActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }
}
