package com.example.jordan.divvyupv12;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity{
        //implements View.OnClickListener{
    public static Button login_button , register_now_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

              OnClickButtonListener();
    }

    public void OnClickButtonListener() {
        register_now_button = (Button)findViewById(R.id.register_now);
        register_now_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.example.jordan.divvyupv12.RegisterActivity");
                        startActivity(intent);
                    }
                }
        );
        login_button = (Button) findViewById(R.id.Login_Button);
        login_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent2 = new Intent("com.example.jordan.divvyupv12.HubActivity");
                        startActivity(intent2);
                    }
                }
        );
    }

//    @Override
//    public void onClick(View v) {
//        switch(v.getId()){
//            case R.id.Login_Button:
//                Intent intent = new Intent("com.example.jordan.divvyupv12.HubActivity");
//                startActivity(intent);
//                break;
//            case R.id.register_now:
//                Intent intent2 = new Intent("com.example.jordan.divvyupv12.RegisterActivity");
//                startActivity(intent2);
//                break;
//        }
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
