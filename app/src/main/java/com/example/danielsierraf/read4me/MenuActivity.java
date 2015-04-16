package com.example.danielsierraf.read4me;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MenuActivity extends ActionBarActivity {

    private String lang_read;
    private String lang_hear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        lang_read = intent.getStringExtra(MainActivity.EXTRA_LANG_READ);
        lang_hear = intent.getStringExtra(MainActivity.EXTRA_LANG_HEAR);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
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

    public void callRealTime(View v){
        //DO SOMETHING
        Toast.makeText(this, "Called real time", Toast.LENGTH_LONG).show();
    }

    public void callLoadPicture(View v){
        //DO SOMETHING
        Toast.makeText(this, "Called load picture", Toast.LENGTH_LONG).show();
    }
}
