package com.example.danielsierraf.read4me;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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
    private Mat src;

    public ImageProcessing(Context context){
        appContext = context;
    }

    public ImageProcessing(Context context, String path){
        appContext = context;
        src = Highgui.imread(path, 0);
    }

    public void setMatGray(String path){
        src = Highgui.imread(path, 0);
    }

    public void setMatColor(String path){
        src = Highgui.imread(path);
    }

    public Bitmap getMatBitmap(){
        Log.d(TAG, "Creating bitmap");
        Bitmap bmp = null;
        if (src != null && !src.empty()){
            bmp = Bitmap.createBitmap(src.cols(), src.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(src, bmp);
            Log.d(TAG, "Seteo Bitmap "+bmp.toString());
        }
        return bmp;
    }

    public void otsuThreshold(){
        //Mat threshold = new Mat();
        Mat blur = new Mat();
        Imgproc.GaussianBlur(src, blur, new Size(5,5), 0);
        //Imgproc.threshold(blur, threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        Imgproc.threshold(blur, src, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //writeImage(src);
        //return threshold;
    }

    public boolean writeImage(){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = "threshold0-Gauss.png";
        //File file = new File(pic_path, filename);
        //File file = new File(new FileHandler().getAlbumPublicStorageDir("Read4Me", ""), filename);
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

    public void resizeImage(double x, double y){
        Size size = new Size(x, y); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Imgproc.resize(src,src,size);//resize image
        //return src;
    }

}
