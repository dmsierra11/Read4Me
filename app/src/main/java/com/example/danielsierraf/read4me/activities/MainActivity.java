package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity {


    public final static boolean TEST_MODE = true;
    public final static String appFolder = new FileHandler().getExternalStorageDir("Read4Me").getPath();

    private final String TAG = "MainActivity";

    private String lang;
    private String langISO3;
    private String countryISO3;
    private Spinner spinner1;
    private Spinner spinner2;
    private Context mContext;

    /*static {
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
                    //mOpenCvCameraView.setOnTouchListener(TextDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                    Log.e(TAG, "Opencv not loaded successfully");
                } break;
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang = this.getResources().getConfiguration().locale.getLanguage();
        langISO3 = this.getResources().getConfiguration().locale.getISO3Language();
        countryISO3 = this.getResources().getConfiguration().locale.getISO3Country();
        Log.d(TAG, "Language: "+lang);
        Log.d(TAG, "Language ISO3: "+langISO3);
        Log.d(TAG, "Country ISO3: " + countryISO3);

        mContext = getApplicationContext();

        //TODO: Thread to train and copy tessdata
        ComponentsInstaller installer = new ComponentsInstaller();
        installer.execute();

        setContentView(R.layout.activity_main);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                R.array.lang_array, android.R.layout.simple_spinner_item);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(dataAdapter);
        spinner2.setAdapter(dataAdapter);

        // Spinner item selection Listener
        //addListenerOnSpinnerItemSelection();

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

    /*// Add spinner data
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

    }*/

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

        //Context context = getApplicationContext();
        FileHandler.setDefaults(getString(R.string.lang_read), lang_read, mContext);
        FileHandler.setDefaults(getString(R.string.lang_hear), lang_hear, mContext);
        FileHandler.setDefaults(getString(R.string.country_hear), country_hear, mContext);

        startActivity(intent);
    }

    public class ComponentsInstaller extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String tessdata_path = appFolder + "/tessdata/";
            String neural_path = appFolder + "/neural_networks/";
            String[] paths = new String[] { appFolder,  tessdata_path, neural_path};
            installFolders(paths);
            installTesseract(tessdata_path);
            installSVM(neural_path);
            return null;
        }
    }

    public void installFolders(String[] paths){

        //Create app directory and tessdata dir
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

    }

    public void installTesseract(String tessdata){

        // lang.traineddata file with the app (in assets folder)
        if (!(new File(tessdata + langISO3 + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = mContext.getAssets();
                InputStream in = assetManager.open("tessdata/" + langISO3 + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(tessdata + langISO3 + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.d(TAG, "Copied " + langISO3 + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + langISO3 + " traineddata " + e.toString());
            }
        }
    }

    public void installSVM(String svm){
        String filename = "SVM.xml";
        // lang.traineddata file with the app (in assets folder)
        if (!(new File(svm + filename)).exists()) {
            try {

                AssetManager assetManager = mContext.getAssets();
                InputStream in = assetManager.open("neural_networks/" + filename);
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(svm + filename);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.d(TAG, "Copied " + filename);
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + filename + " " + e.toString());
            }
        }
    }

}
