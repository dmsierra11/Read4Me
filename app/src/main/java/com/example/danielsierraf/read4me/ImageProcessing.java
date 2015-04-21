package com.example.danielsierraf.read4me;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class ImageProcessing {

    public static final String TAG = "ImageProcessing";
    public static final String appName = "Read4Me";

    private Context appContext;

    public ImageProcessing(Context context){
        appContext = context;
    }

    public void otsuThreshold(String path){
        Mat src = Highgui.imread(path, 0);
        Mat threshold = new Mat();
        Imgproc.threshold(src, threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        writeImage(src);
    }

    public boolean writeImage(Mat src){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = "threshold0.png";
        //File file = new File(pic_path, filename);
        //File file = new File(new FileHandler().getAlbumPublicStorageDir("Read4Me"), filename);
        File file = new File(new FileHandler().getExternalStorageDir(appName), filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, src);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage on "+filename);
        else {
            Log.d(TAG, "Fail writing image to external storage");
            return false;
        }

        return true;
    }

}
