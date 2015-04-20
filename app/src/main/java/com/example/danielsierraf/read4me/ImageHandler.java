package com.example.danielsierraf.read4me;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class ImageHandler {

    private final String TAG = "ImageHandler";
    private Context appContext;

    public ImageHandler(Context context){
        appContext = context;
    }

    public Bitmap getBitmapFromUri(Uri uri){
        Bitmap selectedBitmap = null;
        try {
            selectedBitmap = MediaStore.Images.Media.getBitmap(appContext.getContentResolver(), uri);
        } catch (Exception e){
            Log.e(TAG, "Image not found");
            e.printStackTrace();
        }
        return selectedBitmap;
    }

    public String getImagePath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            Log.e(TAG, "Error getting Uri");
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        //Cursor cursor = managedQuery(uri, projection, null, null, null);
        Cursor cursor = appContext.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }
}
