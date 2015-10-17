package com.example.danielsierraf.read4me.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.example.danielsierraf.read4me.activities.EditPicActivity;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.AppConstant;

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
    //private ImageProcessingInterface mCallback;
    private Context mContext;
    //private ProgressBar progressBar;
    private int selected;
    private boolean first_pass;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        _activity = activity;
        _imagePaths = imagePaths;
        //mCallback = (ImageProcessingInterface) activity;
        first_pass = true;
        mContext = activity.getApplicationContext();
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

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.activity_show_pic, container,
                false);

        ImageView imgDisplay = (ImageView) viewLayout.findViewById(R.id.img_gallery);
        Button btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        Button btnAccept = (Button) viewLayout.findViewById(R.id.btnAccept);
        //progressBar = (ProgressBar) viewLayout.findViewById(R.id.progressBarImgProc);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final String path = _imagePaths.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        imgDisplay.setImageBitmap(bitmap);

        if (first_pass){
            Log.d(TAG, "Image: "+path);
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

        btnAccept.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Intent intent = new Intent(_activity, EditPicActivity.class);
                                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                           intent.putExtra(EditPicActivity.EXTRA_PHOTO_DATA_PATH, path);
                                           /*intent.putExtra(EditPicActivity.EXTRA_ACTION,
                                                   AppConstant.SELECT_PICTURE);*/
                                           mContext.startActivity(intent);
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
}
