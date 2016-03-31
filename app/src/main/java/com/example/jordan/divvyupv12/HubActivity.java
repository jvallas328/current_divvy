package com.example.jordan.divvyupv12;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        //TextView txt = (TextView) findViewById(R.id.filesButton_header);
        //txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //txt = (TextView) findViewById(R.id.groupsButton_header);
        //txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //txt = (TextView) findViewById(R.id.accountButton_header);
        //txt.setPaintFlags(txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hub, menu);
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
            Toast.makeText(HubActivity.this, "Goodbye!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Called when the user clicks the 'Your Files' button
    public void goToFiles(View view){
        Intent intent = new Intent(this, FilesActivity.class);
        startActivity(intent);
    }

    public void featureNotSupported(View view){
        Toast.makeText(HubActivity.this, "Feature Coming Soon!", Toast.LENGTH_LONG).show();
    }

    public void goToAccount(View view){
        Intent intent = new Intent(this, ManageAccountActivity.class);
        startActivity(intent);
    }
}
