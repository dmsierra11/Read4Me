package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import org.opencv.core.Rect;

/**
 * Created by danielsierraf on 8/7/15.
 */
public class OCRFragment extends Fragment {

    private final String TAG = "OCRFragment";
    private final String appName = "Read4Me";

    //private ImageProcessingInterface mCallbackObjects;
    //private OCRInterface mCallbackOCR;
    private Context mContext;
    private String lang_read;
    private DetectTextNative textDetector;
    private ImageProcessing imageProcessing;
    private ScrollView scrollView;
    private TextView textView;
    private ProgressBar progressBar;
    private String message;
    private ImageProcessingInterface mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "OnAttach");
        mContext = activity.getApplicationContext();

        // Make sure that the hosting activity has implemented
        // the correct callback interface.
        try {
            mCallback = (ImageProcessingInterface) activity;
            //mCallbackObjects = (ImageProcessingInterface) activity;
            //mCallbackOCR = (OCRInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TextDetectionInterface");
        }

        //textDetector = mCallbackObjects.getDetectTextObject();
        //imageProcessing = mCallbackObjects.getImageProcObject();
        textDetector = mCallback.getDetectTextObject();
        imageProcessing = mCallback.getImageProcObject();
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
        return inflater.inflate(R.layout.activity_tts, container, false);

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
        //mCallbackObjects = null;
        //mCallbackOCR = null;
        mCallback = null;
    }

    private class OCReader extends AsyncTask<Void, Integer, String>{

        @Override
        protected String doInBackground(Void... params) {
            //textDetector = mCallbackObjects.getDetectTextObject();
            //imageProcessing = mCallbackObjects.getImageProcObject();
            textDetector = mCallback.getDetectTextObject();
            imageProcessing = mCallback.getImageProcObject();
            lang_read = FileHandler.getDefaults(mContext.getString(R.string.lang_read), mContext);
            String path = new FileHandler().getExternalStorageDir(appName).getPath()+
                    "/neural_networks/SVM.xml";


            Log.d(TAG, "Reading with: "+path);
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
            String text = imageProcessing.readPatches(boundingBoxes, lang_read);

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
}
