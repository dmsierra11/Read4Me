package com.example.danielsierraf.read4me.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by danielsierraf on 8/15/15.
 */
public class FullScreenImageAdapter extends PagerAdapter{

    private final String TAG = "FullScreenImageAdapter";

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private LayoutInflater inflater;
    private ImageProcessingInterface mCallback;
    //private Context mContext;
    private ProgressBar progressBar;
    ImageProcessing imageProcessing;
    private int selected;
    private boolean first_pass;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        _activity = activity;
        _imagePaths = imagePaths;
        mCallback = (ImageProcessingInterface) activity;
        first_pass = true;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
        Button btnClose;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.activity_edit_pic, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.img_to_edit);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        Button btnRead = (Button) viewLayout.findViewById(R.id.btn_read_text);
        progressBar = (ProgressBar) viewLayout.findViewById(R.id.progressBarImgProc);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        imgDisplay.setImageBitmap(bitmap);

        if (first_pass){
            Log.d(TAG, "Image: "+_imagePaths.get(position));
            selected = position;
            first_pass = false;
        }

        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           ImageProcessingTask imageProcessingTask = new ImageProcessingTask();
                                           imageProcessingTask.execute();
                                       }
                                   }

        );

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    public class ImageProcessingTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Deteccion de texto (nativo c++)
            DetectTextNative detectText = mCallback.getDetectTextObject();
            imageProcessing = mCallback.getImageProcObject();
            imageProcessing.setMat(_imagePaths.get(selected));
            Mat src = imageProcessing.getMat();
            detectText.detect(src.getNativeObjAddr());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Detection finished");
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            mCallback.notifyDetectionFinished();
        }
    }
}
