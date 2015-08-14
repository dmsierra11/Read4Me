package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 * Created by danielsierraf on 8/8/15.
 */
public class TextDetectionFragment extends Fragment implements View.OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG  = "TextDetectionFragment";

    private Mat mRgba;
    private Scalar CONTOUR_COLOR;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageProcessingInterface mCallback;
    private DetectTextNative textDetector;
    private boolean mDetectionFinished;
    private int[] boxes;

    static {
        System.loadLibrary("opencv_java");
    }

    /**
     * called once the fragment is associated with its activity.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the hosting activity has implemented
        // the correct callback interface.
        try {
            mCallback = (ImageProcessingInterface) activity;
            textDetector = mCallback.getDetectTextObject();
        } catch (Exception e) {
            /*throw new ClassCastException(activity.toString()
                    + " must implement TextDetectionInterface");*/
            e.printStackTrace();
        }

        //imageProcessing = mCallback.getImageProcObject();
    }

    /**
     * called to do initial creation of the fragment.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * creates and returns the view hierarchy associated with the fragment.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.color_blob_detection_surface_view, container, false);

    }

    /**
     * tells the fragment that its activity has completed its own Activity.onCreate()
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOpenCvCameraView = (CameraBridgeViewBase) getActivity().findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setOnTouchListener(TextDetectionFragment.this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        
        mCallback = null;
        textDetector = null;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        //textDetector = new DetectTextNative(am);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        //lang_read = FileHandler.getDefaults(getString(R.string.lang_read), mContext);
        mDetectionFinished = true;
        boxes = null;
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        //Deteccion de texto (nativo c++)
        /*DetectTextNative textDetector = mCallback.getDetectTextObject();
        textDetector.detect(mRgba.getNativeObjAddr());
        int[] boxes = textDetector.getBoundingBoxes();*/
        if (mDetectionFinished){
            mDetectionFinished = false;
            TextDetectionTask textDetectionTask = new TextDetectionTask();
            textDetectionTask.execute();
        }

        //Dibujar rectangulos
        if (boxes != null){
            Rect[] boundingBoxes = new Rect[boxes.length/4];
            int idx = 0;
            for (int i = 0; i < boundingBoxes.length; i++) {
                Rect box = new Rect(0, 0, 0, 0);
                box.x = boxes[idx++];
                box.y = boxes[idx++];
                box.width = boxes[idx++];
                box.height = boxes[idx++];
                boundingBoxes[i] = box;

                Core.rectangle(mRgba, boundingBoxes[i].tl(), boundingBoxes[i].br(), CONTOUR_COLOR, 2);
            }
        }

        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        Mat img = mRgba.clone();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        ImageProcessing imageProcessing = mCallback.getImageProcObject();
        imageProcessing.setMat(img);
        //readText(img);
        mCallback.notifyDetectionFinished();

        return false;
    }

    public class TextDetectionTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Deteccion de texto (nativo c++)
            try {
                //textDetector = mCallback.getDetectTextObject();
                textDetector.detect(mRgba.getNativeObjAddr());
                boxes = textDetector.getBoundingBoxes();
            } catch (Exception e){
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean finished) {
            mDetectionFinished = finished;
        }
    }
}
