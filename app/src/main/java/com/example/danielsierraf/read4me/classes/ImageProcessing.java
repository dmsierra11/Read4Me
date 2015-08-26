package com.example.danielsierraf.read4me.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.danielsierraf.read4me.activities.MainActivity;
import com.example.danielsierraf.read4me.utils.FileHandler;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.Serializable;

/**
 * Created by danielsierraf on 8/7/15.
 */
public class ImageProcessing implements Serializable{

    public static final String appName = "Read4Me";
    public static final String TAG = "ImageProcessing";
    private static final Scalar TEXT_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar TEXT_COLOR = new Scalar(0, 255, 255, 0);

    private Context mContext;
    private Mat src;

    static {
        System.loadLibrary("opencv_java");
    }

    //Constructors
    public ImageProcessing(Context context){
        this.mContext = context;
        src = new Mat();
    }

    public ImageProcessing(Context context, String path){
        this.mContext = context;
        src = Highgui.imread(path, 1);
        preprocess();
    }

    public ImageProcessing(Context context, Mat img){
        this.mContext = context;
        setMat(img);
    }

    //Setters
    public void setMat(Mat img) {
        src = img.clone();
    }

    public void setMat(String path) {
        src = Highgui.imread(path, 1);
        preprocess();
    }

    //Getters
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
            Log.d(TAG, "Seteo Bitmap " + bmp.toString());
        }
        return bmp;
    }

    //Preprocessing
    private void preprocess(){
        int numcols = src.cols();
        if (numcols < 1600) {
            double scale;
            if (numcols < 400) scale = 3;
            else if (numcols >= 400 && numcols < 800) scale = 2.5;
            else if (numcols >= 800 && numcols < 1200) scale = 2;
            else scale = 1.5;
            resizeImage(scale);
        }
    }

    private void resizeImage(double scale) {
        Size size = new Size(0, 0); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Log.d(TAG, "Image size: "+src.rows()+"x"+src.cols());
        Imgproc.resize(src, src, size, scale, scale, Imgproc.INTER_LANCZOS4);//resize image
        Log.d(TAG, "New image size: "+src.rows()+"x"+src.cols());
        //return src;
    }

    //File handling
    private boolean writeImage(String filename){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String ext = ".png";
        filename = filename+ext;
        //String filename = "threshold0-Gauss.png";
        //File file = new File(pic_path, filename);
        //File file = new File(new FileHandler().getAlbumPublicStorageDir("Read4Me", ""), filename);
        //File file = new File(new FileHandler().getExternalStorageDir(MainActivity.appFolder), filename);
        File file = new File(new FileHandler().getExternalStorageDir(appName), filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, src);

        if (bool == true)
            Log.d(TAG, "SUCCESS segment image to external storage on "+filename);
        else {
            Log.d(TAG, "Fail writing image to external storage");
            return false;
        }

        return true;
    }

    //File handling
    private boolean writeSegment(Mat segment, String filename){
        //File pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String ext = ".png";
        filename = filename+ext;
        //String filename = "threshold0-Gauss.png";
        //File file = new File(pic_path, filename);
        //File file = new File(new FileHandler().getAlbumPublicStorageDir("Read4Me", ""), filename);
        //File file = new File(new FileHandler().getExternalStorageDir(MainActivity.appFolder), filename);
        File file = new File(new FileHandler().getExternalStorageDir(appName), filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, segment);

        if (bool == true)
            Log.d(TAG, "SUCCESS segment image to external storage on "+filename);
        else {
            Log.d(TAG, "Fail writing image to external storage");
            return false;
        }

        return true;
    }

    //Image Processing
    public Mat otsuThreshold(Mat img){
        Log.d(TAG, "Threshold");
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(img, img, 5);
        Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        return img;
    }

    public Mat MorphologyTransformation(Mat img){
        //MORPH_RECT
        int morph_elem = 0;
        int morph_size = 63;
        //BLACK_HAT
        int morph_operator = 4;

        Mat element = Imgproc.getStructuringElement(morph_elem,
                new Size(2 * morph_size + 1, 2 * morph_size + 1),
                new Point(morph_size, morph_size));

        // Since MORPH_X : 2,3,4,5 and 6
        int operation = morph_operator + 2;

        Imgproc.morphologyEx( img, img, operation, element );

        return img;
    }

    public Rect[] getBoundingBoxes(int[] boxes){
        Log.d(TAG, "Segmenting...");
        //Mat img = src.clone();
        Rect[] boundingBoxes = new Rect[boxes.length/4];
        Log.d(TAG, "BOXES "+boundingBoxes.length);

        if (MainActivity.TEST_MODE){
            writeImage("original");
        }

        int idx = 0;
        for (int i = 0; i < boundingBoxes.length; i++) {
            Rect box = new Rect(0, 0, 0, 0);
            box.x = boxes[idx++];
            box.y = boxes[idx++];
            box.width = boxes[idx++];
            box.height = boxes[idx++];
            boundingBoxes[i] = box;

            Core.rectangle(src, boundingBoxes[i].tl(), boundingBoxes[i].br(), TEXT_RECT_COLOR, 3);
        }

        Log.d(TAG, "Test Mode: "+MainActivity.TEST_MODE);
        if (MainActivity.TEST_MODE){
            writeSegment(src, "detecccion");
            Log.d(TAG, "Se escribio la imagen en la carpeta de la aplicacion");
        }

        return boundingBoxes;

    }

    public String readPatches(Rect[] boundingBoxes, String lang_read){
        Log.d(TAG, "Reading "+boundingBoxes.length+ "boxes");
        String text = "";
        for (int i=0; i < boundingBoxes.length; i++){
            Rect box = boundingBoxes[i];
            Mat ROI = src.submat(box.y, box.y + box.height, box.x, box.x + box.width);
            Log.d(TAG, "Segmented "+i+" COLS: "+ROI.cols()+" ROWS: "+ROI.rows());
            ROI = otsuThreshold(ROI);
            Log.d(TAG, "Thresholded");
            Mat filtered = MorphologyTransformation(ROI);
            Log.d(TAG, "Morphology transformed");

            if (MainActivity.TEST_MODE) {
                Log.d(TAG, "Writing segment.." + i);
                Log.d(TAG, "Size patch: "+filtered.cols()+"x"+filtered.rows());
                writeSegment(filtered, "" + i);
            }

            //OCR
            Bitmap bmp = getMatBitmap(filtered);
            OCR ocr = new OCR();
            ocr.setLanguage(lang_read);

            String output = ocr.recognizeText(bmp);

            int offset = 10;
            Core.putText(src, output, new Point(box.x + offset, box.y + box.height + offset),
                    Core.FONT_HERSHEY_DUPLEX, 1, TEXT_COLOR, 2);

            //text = text + ocr.recognizeText(bmp) + " ";
            text = text + output + " ";
            Log.d(TAG, "Text: "+text);
        }

        if (MainActivity.TEST_MODE){
            writeImage("original");
        }

        return text;
    }

}
