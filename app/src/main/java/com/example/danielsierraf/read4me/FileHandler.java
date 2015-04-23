package com.example.danielsierraf.read4me;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class FileHandler {

    private Context appContext;

    public FileHandler(){

    }

    public FileHandler(Context context){
        appContext = context;
    }

    public void setAppContext(Context context){
        appContext = context;
    }

    private static final String TAG = "FileHandler";

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getExternalStorageDir(String albumName){
        //String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Read4Me/";

        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        return file;
    }

    /*
        Creates public storage directory for the app
     */
    public File getAlbumPublicStorageDir(String albumName, String type) {
        File file;
        switch (type){
            case "pictures":
                // Get the directory for the user's public pictures directory.
                file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), albumName);
                break;
            case "documents":
                file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), albumName);
                break;
            default:
                // Get the directory for the user's public pictures directory.
                file = new File(Environment.getExternalStoragePublicDirectory(""), albumName);
                break;

        }

        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /*
        Creates Directory only accessible by the app itself
     */
    public File getAlbumPrivateStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

}
