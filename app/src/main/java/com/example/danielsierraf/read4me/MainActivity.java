package com.example.danielsierraf.read4me;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    public final static boolean TEST_MODE = true;
    public final static String appFolder = new FileHandler().getExternalStorageDir("Read4Me").getPath();
    //public final static SVM svmClassifier = new SVM();

    private final String TAG = "MainActivity";

    private String lang;
    private String langISO3;
    private String countryISO3;
    private Spinner spinner1;
    private Spinner spinner2;

    static {
        System.loadLibrary("opencv_java");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                    Log.e(TAG, "Opencv not loaded successfully");
                } break;
            }
        }
    };

    /*static {
        System.loadLibrary("opencv_java");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang = this.getResources().getConfiguration().locale.getLanguage();
        langISO3 = this.getResources().getConfiguration().locale.getISO3Language();
        countryISO3 = this.getResources().getConfiguration().locale.getISO3Country();
        Log.d(TAG, "Language: "+lang);
        Log.d(TAG, "Language ISO3: "+langISO3);
        Log.d(TAG, "Country ISO3: "+countryISO3);

        //TODO: Thread to train and copy tessdata


        setContentView(R.layout.activity_main);

        //System.loadLibrary("opencv_java");

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                R.array.lang_array, android.R.layout.simple_spinner_item);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(dataAdapter);
        spinner2.setAdapter(dataAdapter);

        // Spinner item selection Listener
        addListenerOnSpinnerItemSelection();

        // Button click Listener
        //addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public Activity getActivity(){
        return this;
    }

    // Add spinner data
    public void addListenerOnSpinnerItemSelection(){
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        spinner2.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {

            Toast.makeText(parent.getContext(),
                    "On Item Select : \n" + parent.getItemAtPosition(pos).toString(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }

    private String[] getLanguages(String type){
        String langs[] = new String[]{"en", "es", "fr", "it", "de", "pt"};
        String langsISO3[] = new String[]{"eng", "spa", "fra", "ita", "deu", "por"};
        String countriesISO3[] = new String[]{"USA", "ESP", "FRA", "ITA", "DEU", "BRA"};
        switch (type){
            case "iso3":
                if (langISO3 != "eng") {
                    //Put phone default language first
                    List<String> langsISO3List = new ArrayList<String>(Arrays.asList(langsISO3));
                    langsISO3List.remove(langISO3);
                    langsISO3List.add(0, langISO3);
                    langsISO3 = langsISO3List.toArray(new String[6]);
                }
                return langsISO3;
            case "iso3_country":
                if (countryISO3 != "USA") {
                    //Put phone default country first
                    List<String> countriesISO3List = new ArrayList<String>(Arrays.asList(countriesISO3));
                    countriesISO3List.remove(countryISO3);
                    countriesISO3List.add(0, countryISO3);
                    countriesISO3 = countriesISO3List.toArray(new String[6]);
                }
                return countriesISO3;
            default:
                if (lang != "en"){
                    //Put phone default language first
                    List<String> langsList = new ArrayList<String>(Arrays.asList(langs));
                    langsList.remove(lang);
                    langsList.add(0, lang);
                    langs = langsList.toArray(new String[6]);
                }
                return langs;
        }
    }

    public void sendMessage(View view){
        Intent intent = new Intent(this, MenuActivity.class);

        String lang_read = String.valueOf(spinner1.getSelectedItemPosition());
        String lang_hear = String.valueOf(spinner2.getSelectedItemPosition());

        String[] langsForTess = getLanguages("iso3");
        Log.d(TAG, "Language array ISO3: "+Arrays.toString(langsForTess));
        lang_read = langsForTess[Integer.parseInt(lang_read)];

        String countries[] = getLanguages("iso3_country");
        Log.d(TAG, "Countries array: "+Arrays.toString(countries));
        String country_hear = countries[Integer.parseInt(lang_hear)];

        String languages[] = getLanguages("");
        Log.d(TAG, "Language array:"+Arrays.toString(languages));
        lang_hear = languages[Integer.parseInt(lang_hear)];

        Context context = getApplicationContext();
        FileHandler.setDefaults(getString(R.string.lang_read), lang_read, context);
        FileHandler.setDefaults(getString(R.string.lang_hear), lang_hear, context);
        FileHandler.setDefaults(getString(R.string.country_hear), country_hear, context);

        startActivity(intent);
    }

}
