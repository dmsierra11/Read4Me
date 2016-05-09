package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.example.danielsierraf.read4me.adapters.*;
import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.utils.AppConstant;
import com.example.danielsierraf.read4me.utils.CustomUtils;

import java.util.ArrayList;

/**
 * Created by danielsierraf on 8/15/15.
 */
public class GridViewActivity extends AppCompatActivity {

    private final String TAG = "GridViewActivity";

    private CustomUtils customUtils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        gridView = (GridView) findViewById(R.id.grid_view);

        customUtils = new CustomUtils(this);

        // Initilizing Grid View
        InitilizeGridLayout();

        // loading all image paths from SD card
        imagePaths = customUtils.getFilePaths();

        // Gridview adapter
        adapter = new GridViewImageAdapter(GridViewActivity.this, imagePaths,
                columnWidth);

        // setting grid view adapter
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Log.d(TAG, "Position: "+position);
                Intent i = new Intent(GridViewActivity.this, ShowPicActivity.class);
                i.putExtra("position", position);
                //i.putExtra(EditPicActivity.EXTRA_ACTION, AppConstant.SELECT_PICTURE);
                startActivity(i);
            }
        });
    }

    private void InitilizeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstant.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((customUtils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS);

        gridView.setNumColumns(AppConstant.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }
}
