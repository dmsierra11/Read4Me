package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.MenuActivity;
import com.example.danielsierraf.read4me.utils.AppConstant;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.CustomCameraInterface;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.Read4MeApp;
import com.example.danielsierraf.read4me.views.NativeCameraCustomView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by danielsierraf on 8/8/15.
 */
public class TextDetectionFragment extends Fragment implements View.OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG  = "TextDetectionFragment";

    private Mat mRgba;
    private Mat mRgbaOriginal;
    private Scalar CONTOUR_COLOR;
    private NativeCameraCustomView mOpenCvCameraView;
    private Button btn_tap2start;
    private ImageProcessingInterface mCallback;
    private DetectTextNative textDetector;
    private ImageProcessing imageProcessing;
    private boolean mDetectionFinished;
    private int[] boxes;
    private boolean mStart;
    private Context mContext;

    private SubMenu mColorEffectsMenu;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mResolutionMenu;
    private List<Camera.Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private int mAction;
    private CustomCameraInterface mCallbackPicture;

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

        Bundle args_ = getArguments();
        mAction = args_.getInt("action");

        // Make sure that the hosting activity has implemented
        // the correct callback interface.
        try {
            mCallback = (ImageProcessingInterface) activity;
            mCallbackPicture = (CustomCameraInterface) activity;
            textDetector = mCallback.getDetectTextObject();
            imageProcessing = mCallback.getImageProcObject();
            mStart = false;
            mContext = Read4MeApp.getInstance();
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
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaOriginal = new Mat(height, width, CvType.CV_8UC4);
        //textDetector = new DetectTextNative(am);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        //lang_read = FileHandler.getDefaults(getString(R.string.lang_read), mContext);
        mDetectionFinished = true;
        boxes = null;

        if (mOpenCvCameraView.isPictureSizeSupported())
            mOpenCvCameraView.setPictureSize();

        //Camera.Size resolution = mOpenCvCameraView.getResolution();
        //Log.d(TAG, "Resolution: " + resolution.height + "x" + resolution.width);
        //Camera.Size bestSize = mOpenCvCameraView.findBestSize();
        //Log.d(TAG, "Best size: " + bestSize.height + "x" + bestSize.width);
        //mOpenCvCameraView.setResolution(bestSize);
        Camera.Size new_resolution = mOpenCvCameraView.getResolution();
        Toast.makeText(mContext, new_resolution.width+"x"+new_resolution.height,
                Toast.LENGTH_SHORT).show();

        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            mOpenCvCameraView.setFlash();
        else
            Log.e(TAG, "Flash not available");

        if(mOpenCvCameraView.isFocusModeSupported()){
            List<String> focus_modes = mOpenCvCameraView.getFocusModeSupported();
            Log.d(TAG, "Focus modes: "+ Arrays.toString(focus_modes.toArray()));
        } else Log.e(TAG, "Focus mode not supported");

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mRgbaOriginal = mRgba.clone();

        if (mStart){
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
        }

        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mStart){
            imageProcessing.setMat(mRgbaOriginal);
            if (mAction == MenuActivity.REAL_TIME_ACTION){
                //imageProcessing = mCallback.getImageProcObject();
                //imageProcessing.setMat(mRgba);
                //imageProcessing.setMat(mRgbaOriginal);
                mCallback.notifyDetectionFinished(imageProcessing, textDetector);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                File file = new FileHandler().getAlbumPublicStorageDir(AppConstant.APP_NAME, "pictures");
                //String fileName = file.getPath() + "/" + currentDateandTime + ".png";
                //mOpenCvCameraView.takePicture(fileName, mCallbackPicture);
                takePicture(currentDateandTime, file.getPath());
            }
        } else {
            mStart = true;
            btn_tap2start.setVisibility(View.INVISIBLE);
        }

        return false;
    }

    private void takePicture(String currentDateandTime, String file) {
        imageProcessing.writeImage(currentDateandTime, file, imageProcessing.getMat());
        String fileName = file + "/" + currentDateandTime + ".png";
        mCallbackPicture.notifyPictureTaken(fileName);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(mContext, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Camera.Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(mContext, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
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
