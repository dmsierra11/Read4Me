package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.adapters.FullScreenImageAdapter;
import com.example.danielsierraf.read4me.utils.CustomUtils;

/**
 * Created by danielsierraf on 10/8/15.
 */
public class ShowPicActivity extends Activity {

    private static final String TAG = "ShowPicActivity";

    private AssetManager am;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        am = getAssets();
        mContext = getApplicationContext();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        CustomUtils customUtils = new CustomUtils(getApplicationContext());

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);
        Log.d(TAG, "Position clicked: " + position);

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(ShowPicActivity.this,
                customUtils.getFilePaths());

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
