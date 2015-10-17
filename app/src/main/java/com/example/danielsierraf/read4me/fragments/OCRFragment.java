package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.GoogleTranslate;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.Read4MeApp;

import org.opencv.core.Rect;

import java.util.zip.Inflater;

/**
 * Created by danielsierraf on 8/7/15.
 */
public class OCRFragment extends Fragment {

    private final String TAG = "OCRFragment";
    private final String appName = "Read4Me";

    private Context mContext;
    //private String lang_read;
    //private DetectTextNative textDetector;
    //private ImageProcessing imageProcessing;
    private ScrollView scrollView;
    private TextView textView;
    //private ImageView imageView;
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

        //ImageProcessing imageProcessing = ImageProcessor.getImageProcessing();
        //this.imageProcessing = imageProcessing;

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
        //imageView = (ImageView) getActivity().findViewById(R.id.ocr_image);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBarTTS);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    // Null out mCallbackObjects
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        //textDetector = null;
        //imageProcessing = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class OCReader extends AsyncTask<Void, Integer, String>{

       ImageProcessing imageProcessing;

        @Override
        protected String doInBackground(Void... params) {

            String lang_read = FileHandler.getDefaults(mContext.getString(R.string.lang_read), mContext);
            String lang_hear = FileHandler.getDefaults(mContext.getString(R.string.lang_hear), mContext);
            String path = new FileHandler().getExternalStorageDir(appName).getPath()+
                    "/neural_networks/SVM.xml";

            DetectTextNative textDetector = mCallback.getDetectTextObject();
            imageProcessing = mCallback.getImageProcObject();
            String text = "";
            if (textDetector != null){
                int[] boundBoxes = textDetector.getBoundingBoxes();

                if (boundBoxes != null){
                    int numBoxes = textDetector.getBoundingBoxes().length/4;
                    Log.d(TAG, "Filtering "+numBoxes+" boxes");
                } else {
                    Log.d(TAG, "No bounding boxes");
                }

                textDetector.read(path);
                int[] boxes = textDetector.getBoxesWords();

                //finalizar detecccion
                Log.d(TAG, "Finalizando deteccion");
                try {
                    textDetector.finalize();
                } catch (Throwable throwable) {
                    Log.e(TAG, "Error finalizando");
                    throwable.printStackTrace();
                }
                Log.d(TAG, "Deteccion finalizada");

                //show bounding boxes
                Rect[] boundingBoxes = imageProcessing.getBoundingBoxes(boxes);
                //OCR
                text = imageProcessing.readPatches(boundingBoxes, lang_read);
            } else {
                text = imageProcessing.processDocument(lang_read);
            }

            String lang_readISO3 = convertISO3ToNormal(lang_read);
            if (!lang_readISO3.equals(lang_hear)){
                GoogleTranslate translator = new GoogleTranslate("AIzaSyCKBRreFjCI4diZxOl1NB_LwVO6HR5Uelc");
                text = translator.translate(text, lang_readISO3, lang_hear);
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
            //imageView.setImageBitmap(imageProcessing.getMatBitmap());
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
