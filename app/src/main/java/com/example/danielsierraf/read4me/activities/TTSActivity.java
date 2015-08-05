package com.example.danielsierraf.read4me.activities;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.danielsierraf.read4me.classes.FileHandler;
import com.example.danielsierraf.read4me.R;

import java.util.Locale;


public class TTSActivity extends ActionBarActivity implements TextToSpeech.OnInitListener{

    public static final String EXTRA_TEXT = "com.example.danielsierraf.TTSActivity.EXTRA_TEXT";

    private static final int MY_DATA_CHECK_CODE = 1234;

    private final String TAG = "TTSActivity";

    private String message;
    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        Intent intent = getIntent();
        message = intent.getStringExtra(EXTRA_TEXT);
        TextView textView = (TextView) findViewById(R.id.ocr_text);
        textView.setText(message);

        checkTTSResource();
    }

    @Override
    public void onDestroy()
    {
        // Don't forget to shutdown!
        if (mTts != null)
        {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tt, menu);
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

    @Override
    public void onInit(int status) {
        //mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "");
        Context context = getApplicationContext();
        String lang_hear = FileHandler.getDefaults(getString(R.string.lang_hear), context);
        String country_hear = FileHandler.getDefaults(getString(R.string.country_hear), context);
        Log.d(TAG, "Hearing " + lang_hear + ", " + country_hear);
        Locale loc = new Locale (lang_hear, country_hear);
        mTts.setLanguage(loc);
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void checkTTSResource(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
}
