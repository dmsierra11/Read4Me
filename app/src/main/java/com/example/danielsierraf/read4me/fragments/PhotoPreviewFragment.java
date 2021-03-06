package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.MenuActivity;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.CustomCameraInterface;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

/**
 * Created by danielsierraf on 8/21/15.
 */
public class PhotoPreviewFragment extends Fragment {

    private static final String TAG = "PhotoPreviewFragment";

    private String photo_path;
    private Context mContext;
    private CustomCameraInterface mCallbackCamera;
    private ImageProcessingInterface mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
        mCallback = (ImageProcessingInterface) activity;
        mCallbackCamera = (CustomCameraInterface) activity;
        Bundle args_ = getArguments();
        photo_path = args_.getString("photo_path");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preview_pic, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ImageProcessing imageProcessing = mCallback.getImageProcObject();

        ImageView image = (ImageView) getActivity().findViewById(R.id.preview_image);
        //Log.d(TAG, "Image: "+photo_path);
        //image.setImageURI(Uri.parse(photo_path));
        image.setImageBitmap(imageProcessing.getMatBitmap());

        Button btn_retake = (Button) getActivity().findViewById(R.id.btn_take_another);
        btn_retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbackCamera.retake();
            }
        });

        Button btn_done = (Button) getActivity().findViewById(R.id.btn_pic_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageProcessing.writeImage(photo_path);
                Intent intent = new Intent(mContext, MenuActivity.class);
                startActivity(intent);
            }
        });

        Button btn_save = (Button) getActivity().findViewById(R.id.btn_save_and_take);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageProcessing.writeImage(photo_path);
                mCallbackCamera.retake();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
