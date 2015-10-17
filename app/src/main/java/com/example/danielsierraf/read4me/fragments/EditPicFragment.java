package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;
import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.EditPicActivity;
import com.example.danielsierraf.read4me.activities.MainActivity;
import com.example.danielsierraf.read4me.classes.ImageProcessing;

import java.io.File;

/**
 * Created by danielsierraf on 10/14/15.
 */
public class EditPicFragment extends Fragment {
    // Static final constants
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
    private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
    private static final int ON_TOUCH = 1;

    // Instance variables
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

    Bitmap croppedImage;
    private EditPicActivity mCallback;
    private CropImageView cropImageView;
    private Context mContext;
    //private Activity mActivity;

    public interface PicEditor {
        public Bitmap getPicture();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
        //mActivity = activity;

        try {
            mCallback = (EditPicActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ImageProcessingInterface");
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.fragment_crop_image);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null){
            mAspectRatioX = savedInstanceState.getInt(ASPECT_RATIO_X);
            mAspectRatioY = savedInstanceState.getInt(ASPECT_RATIO_Y);
        }

        //Bundle args_ =  getArguments();
        //String path = args_.getString(MainActivity.EXTRA_PHOTO_PATH);

        // Sets fonts for all
        //Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Thin.ttf");
        //ViewGroup root = (ViewGroup) getActivity().findViewById(R.id.mylayout);
        //setFont(root, mFont);

        // Initialize components of the app
        cropImageView = (CropImageView) getActivity().findViewById(R.id.CropImageView);
        final Bitmap photo = mCallback.getPicture();
        cropImageView.setImageBitmap(photo);
        //cropImageView.setImageResource(R.drawable.butterfly);

        //ImageView imageView = (ImageView) getActivity().findViewById(R.id.croppedImageView);
        //imageView.setImageBitmap(photo);

        //Sets the rotate button
        final Button rotateButton = (Button) getActivity().findViewById(R.id.btn_rotate);
        rotateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
            }
        });

        // Sets initial aspect ratio to 10/10, for demonstration purposes
        cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

        final Button cropButton = (Button) getActivity().findViewById(R.id.btn_read_text);
        cropButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                ImageProcessing imageProcessing = new ImageProcessing(mContext, croppedImage);
                mCallback.notifyDetectionFinished(imageProcessing, null);
                //ImageView croppedImageView = (ImageView) getActivity().findViewById(R.id.croppedImageView);
                //croppedImageView.setImageBitmap(croppedImage);
                //mCallback.onFinishedEditing(croppedImage);
            }
        });
    }

    // Saves the state upon rotating the screen/restarting the activity
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
        bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
    }

    /*
     * Sets the font on all TextViews in the ViewGroup. Searches recursively for
     * all inner ViewGroups as well. Just add a check for any other views you
     * want to set as well (EditText, etc.)
     */
    public void setFont(ViewGroup group, Typeface font) {
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
                ((TextView) v).setTypeface(font);
            } else if (v instanceof ViewGroup)
                setFont((ViewGroup) v, font);
        }
    }
}
