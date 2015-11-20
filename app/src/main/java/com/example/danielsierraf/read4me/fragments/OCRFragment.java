package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.MainActivity;
import com.example.danielsierraf.read4me.classes.GoogleTranslate;
import com.example.danielsierraf.read4me.utils.AppConstant;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.Read4MeApp;

import java.util.Date;

/**
 * Created by danielsierraf on 8/7/15.
 */
public class OCRFragment extends Fragment {

    private final String TAG = "OCRFragment";

    private Context mContext;
    private ScrollView scrollView;
    private TextView textView;
    private ProgressBar progressBar;
    private String message;
    private ImageProcessingInterface mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "OnAttach");
        mContext = Read4MeApp.getInstance();

        // Make sure that the hosting activity has implemented
        // the correct callback interface.
        try {
            mCallback = (ImageProcessingInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TextDetectionInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        // Preserve across reconfigurations
        setRetainInstance(true);
        setHasOptionsMenu(true);

        OCReader ocrRead = new OCReader();
        ocrRead.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView");
        return inflater.inflate(R.layout.ocr, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "OnActivityCreated");

        scrollView = (ScrollView) getActivity().findViewById(R.id.scrollView);
        scrollView.setEnabled(false);
        textView = (TextView) getActivity().findViewById(R.id.ocr_text);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBarTTS);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    // Null out mCallbackObjects
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        //imageProcessing = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_langs) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private class OCReader extends AsyncTask<Void, Integer, String>{

       //ImageProcessing imageProcessing;

        @Override
        protected String doInBackground(Void... params) {

            String lang_read = FileHandler.getDefaults(mContext.getString(R.string.lang_read), mContext);
            String lang_hear = FileHandler.getDefaults(mContext.getString(R.string.lang_hear), mContext);
            /*String path = new FileHandler().getExternalStorageDir(appName).getPath()+
                    "/neural_networks/SVM.xml";*/

            //DetectTextNative textDetector = mCallback.getDetectTextObject();
            ImageProcessing imageProcessing = mCallback.getImageProcObject();

            String text = imageProcessing.getText();
            if (text == "")
                text = imageProcessing.processDocument(lang_read);

            String lang_readISO3 = convertISO3ToNormal(lang_read);
            if (!lang_readISO3.equals(lang_hear)){
                try {
                    Date dateSTranslation = new Date();
                    //translation
                    GoogleTranslate translator = new GoogleTranslate("AIzaSyCKBRreFjCI4diZxOl1NB_LwVO6HR5Uelc");
                    text = translator.translate(text, lang_readISO3, lang_hear);
                    Date dateFTranslation = new Date();
                    long timeTranslation = dateFTranslation.getTime() - dateSTranslation.getTime();
                    Log.d(AppConstant.TAG_TIME_ELAPSED, "Time translation: "+timeTranslation);
                } catch (Exception ex){
                    text = getString(R.string.no_internet);
                    ex.printStackTrace();
                }

            }

            return text;
        }

        @Override
        protected void onPostExecute(String text) {
            Log.d(TAG, "OCRed: " + text);
            message = text;
            scrollView.setEnabled(true);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            textView.setText(message);
            mCallback.notifyOCRFinished(text);
        }
    }

    public String convertISO3ToNormal(String lang){
        String language = "";
        switch (lang){
            case "eng":
                language = "en";
                break;
            case "spa":
                language = "es";
                break;
            case "fra":
                language = "fr";
                break;
            case "ita":
                language = "it";
                break;
            case "deu":
                language = "de";
                break;
            case "por":
                language = "pt";
                break;
        }
        return language;
    }
}
