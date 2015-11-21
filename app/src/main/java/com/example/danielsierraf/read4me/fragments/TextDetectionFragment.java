package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.MainActivity;
import com.example.danielsierraf.read4me.classes.OCR;
import com.example.danielsierraf.read4me.classes.Word;
import com.example.danielsierraf.read4me.utils.AppConstant;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.Read4MeApp;
import com.example.danielsierraf.read4me.views.NativeCameraCustomView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by danielsierraf on 8/8/15.
 */
public class TextDetectionFragment extends Fragment implements View.OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG  = "TextDetectionFragment";

    private Mat mRgba;
    private Scalar CONTOUR_COLOR;
    private Scalar TEXT_COLOR;
    private NativeCameraCustomView mOpenCvCameraView;
    private Button btn_tap2start;
    private ImageProcessingInterface mCallback;
    private DetectTextNative textDetector;
    private ImageProcessing imageProcessing;
    private boolean mDetectionFinished;
    private int[] boxes;
    private boolean mStart;
    private Context mContext;

//    private SubMenu mColorEffectsMenu;
//    private MenuItem[] mEffectMenuItems;
//    private SubMenu mResolutionMenu;
//    private List<Camera.Size> mResolutionList;
//    private MenuItem[] mResolutionMenuItems;
//    private int mAction;
    private String lang_read;
    private Word[] words;
    private OCR ocr;
    private TextRecognitionTask textRecognitionTask;
    private TextDetectionTask textDetectionTask;

    static {
        System.loadLibrary("opencv_java");
    }

    private Date dateSDetect;
    private TextView popupText;
    private Button popupButtonDiscard;
    private LinearLayout layoutOfPopup;
    private PopupWindow popupMessage;
    private Button popupButtonSave;
    private boolean mflashOn;

    /**
     * called once the fragment is associated with its activity.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        popupText = new TextView(activity);
        popupButtonDiscard = new Button(activity);
        popupButtonSave = new Button(activity);
        layoutOfPopup = new LinearLayout(activity);

        // Make sure that the hosting activity has implemented
        // the correct callback interface.
        try {
            mCallback = (ImageProcessingInterface) activity;
        } catch (Exception e) {
            /*throw new ClassCastException(activity.toString()
                    + " must implement TextDetectionInterface");*/
            e.printStackTrace();
        }

        textDetector = mCallback.getDetectTextObject();
        imageProcessing = mCallback.getImageProcObject();
        mStart = false;
        mContext = Read4MeApp.getInstance();
        lang_read = FileHandler.getDefaults(mContext.getString(R.string.lang_read), mContext);
        ocr = new OCR(lang_read);
    }

    /**
     * called to do initial creation of the fragment.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
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
        return inflater.inflate(R.layout.text_detection_surface_view, container, false);

    }

    /**
     * tells the fragment that its activity has completed its own Activity.onCreate()
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOpenCvCameraView = (NativeCameraCustomView) getActivity().findViewById(R.id.text_detection_java_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setOnTouchListener(TextDetectionFragment.this);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        btn_tap2start = (Button) getActivity().findViewById(R.id.btn_tap2start);
        btn_tap2start.setVisibility(View.VISIBLE);
        btn_tap2start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStart = true;
                btn_tap2start.setVisibility(View.INVISIBLE);
            }
        });
        btn_tap2start.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
        textDetector = null;
        imageProcessing = null;
        mContext = null;
        ocr.finalize();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        TEXT_COLOR = new Scalar(0,0,255,255);
        mDetectionFinished = true;
        boxes = null;
        words = null;
//        lang_read = FileHandler.getDefaults(mContext.getString(R.string.lang_read), mContext);
//        ocr = new OCR(lang_read);

        //Set biggest size
        List<Camera.Size> sizes = mOpenCvCameraView.getResolutionList();
        mOpenCvCameraView.setResolution(sizes.get(0));
        Camera.Size new_resolution = mOpenCvCameraView.getResolution();

        String focusMode = "disabled";
        if(mOpenCvCameraView.isFocusModeSupported()){
            List<String> focus_modes = mOpenCvCameraView.getFocusModeSupported();
            Log.d(TAG, "Focus modes: " + Arrays.toString(focus_modes.toArray()));
            //mOpenCvCameraView.setFocusable(true);
            focusMode = focus_modes.get(0);
            mOpenCvCameraView.setFocusMode(focusMode);
        } else Log.e(TAG, "Focus mode not supported");

        Toast.makeText(mContext, new_resolution.width+"x"+new_resolution.height +
                        ", Focus mode: "+focusMode, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
//        ocr.finalize();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mStart){

            if (mDetectionFinished){
                mDetectionFinished = false;
                dateSDetect = new Date();
                textDetectionTask = new TextDetectionTask();
                textDetectionTask.execute();
            }

            if (words != null){
                for (int i = 0; i < words.length; i++){
                    try {
                        int offset = 10;
                        //show regions detected
                        Core.rectangle(mRgba, words[i].getBox().tl(), words[i].getBox().br(), CONTOUR_COLOR, 2);
                        //put text recognized on frame
                        Core.putText(mRgba, words[i].getText(), new Point(words[i].getBox().x + offset
                                        , words[i].getBox().y + words[i].getBox().height + offset),
                                Core.FONT_HERSHEY_PLAIN, 1, TEXT_COLOR, 1);
                    } catch (Exception e){
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
                runPopupThread();
            } else if (boxes != null){
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

        }

        return mRgba;
    }

    public void runPopupThread(){
        new Thread() {
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStart = false;
                            if (popupMessage == null)
                                popupInit();
                            else bringPopupToFront();
                        }
                    });
                } catch (Exception e){
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        }.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (!mStart){
            mStart = true;
            btn_tap2start.setVisibility(View.INVISIBLE);
        }

        return false;
    }

    private void takePicture(String currentDateandTime, String file) {
        String fileName = file + "/" + currentDateandTime + ".png";
        imageProcessing.writeImage(fileName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        mflashOn = false;
        String flashStatus = "Off";
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            flashStatus = "Not available";
        menu.add("Flash: "+flashStatus);
        //List<String> effects = mOpenCvCameraView.getEffectList();

        /*if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }*/

        /*if (effects != null) {
            mColorEffectsMenu = menu.addSubMenu("Color Effect");
            mEffectMenuItems = new MenuItem[effects.size()];

            int idx = 0;
            ListIterator<String> effectItr = effects.listIterator();
            while (effectItr.hasNext()) {
                String element = effectItr.next();
                mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
                idx++;
            }

            mResolutionMenu = menu.addSubMenu("Resolution");
            mResolutionList = mOpenCvCameraView.getResolutionList();
            mResolutionMenuItems = new MenuItem[mResolutionList.size()];

            ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
            idx = 0;
            while (resolutionItr.hasNext()) {
                Camera.Size element = resolutionItr.next();
                mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                        Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
                idx++;
            }
        }*/

        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 0){
            if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                mflashOn = !mflashOn;
                mOpenCvCameraView.setFlash(mflashOn);
            } else {
                Toast.makeText(mContext, "Flash not available", Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.action_change_langs) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }
//        if (item.getGroupId() == 1)
//        {
//            mOpenCvCameraView.setEffect((String) item.getTitle());
//            Toast.makeText(mContext, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
//        }
//        else if (item.getGroupId() == 2)
//        {
//            int id = item.getItemId();
//            Camera.Size resolution = mResolutionList.get(id);
//            mOpenCvCameraView.setResolution(resolution);
//            resolution = mOpenCvCameraView.getResolution();
//            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
//            Toast.makeText(mContext, caption, Toast.LENGTH_SHORT).show();
//        }

        return super.onOptionsItemSelected(item);
    }

    public void popupInit() {
        popupButtonDiscard.setText("Descartar");
        popupButtonSave.setText("Aceptar");
        popupText.setText("Aceptar resultado?");
        popupText.setPadding(0, 0, 0, 20);
        layoutOfPopup.setOrientation(LinearLayout.HORIZONTAL);
        layoutOfPopup.addView(popupText);
        layoutOfPopup.addView(popupButtonDiscard);
        layoutOfPopup.addView(popupButtonSave);

        popupButtonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartActivities();
            }
        });
        popupButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                File file = new FileHandler().getAlbumPublicStorageDir(AppConstant.APP_NAME, "pictures");
                takePicture(currentDateandTime, file.getPath());
                restartActivities();
            }
        });

        popupMessage = new PopupWindow(layoutOfPopup, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupMessage.setContentView(layoutOfPopup);

        bringPopupToFront();
    }

    private void bringPopupToFront(){
        popupMessage.showAtLocation(getActivity().findViewById(R.id.text_detection_java_surface_view)
                , Gravity.CENTER, 0, 0);
        mOpenCvCameraView.disableView();
    }

    private void restartActivities(){
        mOpenCvCameraView.enableView();
        mDetectionFinished = true;
        popupMessage.dismiss();
    }

    public class TextDetectionTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Deteccion de texto (nativo c++)
            try {
                Mat img = mRgba.clone();

                textDetector.detect(img.getNativeObjAddr());
                boxes = textDetector.getBoundingBoxes();

                imageProcessing.setMat(img);
            } catch (Exception e){
                Log.e(TAG, "Error en la detección");
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean finished) {
            //mDetectionFinished = finished;

            Date dateFDetect = new Date();
            long timeDetect = dateFDetect.getTime() - dateSDetect.getTime();
            Log.d(AppConstant.TAG_TIME_ELAPSED, "Detection: "+timeDetect);

            textRecognitionTask = new TextRecognitionTask();
            textRecognitionTask.execute();
        }
    }

    public class TextRecognitionTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Deteccion de texto (nativo c++)
            try {
                Rect[] boundingBoxes = imageProcessing.getBoundingBoxes(boxes);
                //OCR
                words = imageProcessing.readPatches(boundingBoxes, ocr);
            } catch (Exception e){
                Log.e(TAG, "Error en la detección");
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }

            return true;
        }
    }
}
