package com.example.danielsierraf.read4me;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class ImageProcessing {

    public static final String TAG = "ImageProcessing";
    public static final String appName = "Read4Me";
    private static final Scalar TEXT_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private Context appContext;
    private Mat src;

    public ImageProcessing(Context context){
        appContext = context;
    }

    public ImageProcessing(Context context, String path){
        appContext = context;
        src = Highgui.imread(path, 1);
    }

    public ImageProcessing(Context context, Mat img){
        appContext = context;
        src = img;
    }

    public void setMatColor(String path){
        src = Highgui.imread(path, 1);
    }

    public void setMat(Mat img) { src = img; }

    public Mat getMat(){ return src; }

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

    public Bitmap getMatBitmap(Mat img){
        Log.d(TAG, "Creating bitmap");
        Bitmap bmp = null;
        if (img != null && !img.empty()){
            bmp = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(img, bmp);
            Log.d(TAG, "Seteo Bitmap "+bmp.toString());
        }
        return bmp;
    }

    public void otsuThreshold(){
        //Mat threshold = new Mat();
        Mat blur = new Mat();
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(src, blur, new Size(5,5), 0);
        //Imgproc.threshold(blur, threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        Imgproc.threshold(blur, src, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //writeImage(src);
        //return threshold;
    }

    public Mat otsuThreshold(Mat img){
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //Core.bitwise_not(img, img);
        return img;
    }

    public boolean writeImage(String filename){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String ext = ".png";
        filename = filename+ext;
        //String filename = "threshold0-Gauss.png";
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

    private boolean writeSegment(Mat segment, String filename){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String ext = ".png";
        filename = filename+ext;
        //String filename = "threshold0-Gauss.png";
        //File file = new File(pic_path, filename);
        //File file = new File(new FileHandler().getAlbumPublicStorageDir("Read4Me", ""), filename);
        File file = new File(new FileHandler().getExternalStorageDir(appName), filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, segment);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage on "+filename);
        else {
            Log.d(TAG, "Fail writing image to external storage");
            return false;
        }

        return true;
    }

    public void resizeImage(double x, double y) {
        Size size = new Size(x, y); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Imgproc.resize(src, src, size);//resize image
        //return src;
    }

    public void resizeImage(double scale) {
        Size size = new Size(0, 0); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Imgproc.resize(src, src, size, scale, scale, Imgproc.INTER_LANCZOS4);//resize image
        //return src;
    }

    public ArrayList<Mat> segment(int[] boxes){
        Mat img = src.clone();
        ArrayList<Mat> segments = new ArrayList<Mat>();
        Rect[] boundingBoxes = new Rect[boxes.length/4];
        int idx = 0;
        for (int i = 0; i < boundingBoxes.length; i++) {
            Rect box = new Rect(0, 0, 0, 0);
            box.x = boxes[idx++];
            box.y = boxes[idx++];
            box.width = boxes[idx++];
            box.height = boxes[idx++];
            boundingBoxes[i] = box;

            Mat ROI = img.submat(box.y, box.y + box.height, box.x, box.x + box.width);
            Log.d(TAG, "Segment "+i+" COLS: "+ROI.cols()+" ROWS: "+ROI.rows());
            if (ROI.rows() < 30){
                resizeImage(1.5);
            }
            Log.d(TAG, "threshold");
            Mat thresh = otsuThreshold(ROI);
            segments.add(thresh);
            writeSegment(thresh, ""+i);

            Core.rectangle(img, boundingBoxes[i].tl(), boundingBoxes[i].br(), TEXT_RECT_COLOR, 3);
        }

        writeSegment(img, "prueba");
        Log.d(TAG, "Se escribio la imagen en la carpeta de la aplicacion");

        return segments;
    }

}
