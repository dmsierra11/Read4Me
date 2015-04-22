package com.example.danielsierraf.read4me;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class OCR {
    private static final String TAG = "OCR";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Read4Me/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    private String lang;
    private Context appContext;

    public OCR(Context myContext){
        appContext = myContext;
        lang = "eng";
        initialize();
    }

    public OCR(Context myContext, String _lang){
        appContext = myContext;
        lang = _lang;
        initialize();
    }

    public void setLanguage(String _lang){
        lang = _lang;
    }

    public String getLanguage(){
        return lang;
    }

    public void initialize(){
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = appContext.getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.d(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }

    public String recognizeText(Bitmap bitmap){
        String recognizedText = "";
        try {
            Log.d(TAG, "Before baseApi");

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmap);

            recognizedText = baseApi.getUTF8Text();
            Log.d(TAG, "RECOGNIZED TEXT: "+recognizedText);

            baseApi.end();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage());
            // TODO: handle exception
        }

        return recognizedText;
    }
}
