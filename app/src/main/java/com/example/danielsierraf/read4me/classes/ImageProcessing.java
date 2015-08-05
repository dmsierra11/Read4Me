package com.example.danielsierraf.read4me.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.danielsierraf.read4me.activities.MainActivity;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielsierraf on 4/20/15.
 */
public class ImageProcessing {

    public static final String TAG = "ImageProcessing";
    public static final String appName = "Read4Me";
    private static final Scalar TEXT_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private Context context;
    private Mat src;
    //private ArrayList<Mat> segments;

    //Constructors
    public ImageProcessing(Context context){
        this.context = context;
        src = new Mat();
        //segments = new ArrayList<Mat>();
    }

    public ImageProcessing(Context context, String path){
        this.context = context;
        src = Highgui.imread(path, 1);
        //segments = new ArrayList<Mat>();
        preprocess();
    }

    public ImageProcessing(Context context, Mat img){
        this.context = context;
        //segments = new ArrayList<Mat>();
        setMat(img);
    }

    //Setters
    public void setMat(Mat img) {
        src = img;
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
            Log.d(TAG, "Seteo Bitmap "+bmp.toString());
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

    private void resizeImage(double x, double y) {
        Size size = new Size(x, y); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Log.d(TAG, "Image size: "+src.rows()+"x"+src.cols());
        Imgproc.resize(src, src, size);//resize image
        Log.d(TAG, "New image size: "+src.rows()+"x"+src.cols());
        //return src;
    }

    private void resizeImage(double scale) {
        Size size = new Size(0, 0); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Log.d(TAG, "Image size: "+src.rows()+"x"+src.cols());
        Imgproc.resize(src, src, size, scale, scale, Imgproc.INTER_LANCZOS4);//resize image
        Log.d(TAG, "New image size: "+src.rows()+"x"+src.cols());
        //return src;
    }

    public Mat resizeImage(Mat img, double x, double y){
        Size size = new Size(x, y); //the dst image size,e.g.100x100
        //Mat dst = new Mat();
        Log.d(TAG, "Image size: "+img.rows()+"x"+img.cols());
        Imgproc.resize(img, img, size);//resize image
        Log.d(TAG, "New image size: "+img.rows()+"x"+img.cols());
        return img;
    }

    //File handling
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
            Log.d(TAG, "SUCCESS segment image to external storage on "+filename);
        else {
            Log.d(TAG, "Fail writing image to external storage");
            return false;
        }

        return true;
    }

    /*public void otsuThreshold(){
        Log.d(TAG, "Threshold Gaussian");
        //Mat threshold = new Mat();
        Mat blur = new Mat();
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(src, blur, new Size(5,5), 0);
        //Imgproc.threshold(blur, threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        Imgproc.threshold(blur, src, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //writeImage(src);
        //return threshold;
    }*/

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
        Mat img = src.clone();
        Log.d(TAG, "cloned");
        Rect[] boundingBoxes = new Rect[boxes.length/4];
        Log.d(TAG, "BOXES "+boundingBoxes.length);
        int idx = 0;
        for (int i = 0; i < boundingBoxes.length; i++) {
            Rect box = new Rect(0, 0, 0, 0);
            box.x = boxes[idx++];
            box.y = boxes[idx++];
            box.width = boxes[idx++];
            box.height = boxes[idx++];
            boundingBoxes[i] = box;

            Core.rectangle(img, boundingBoxes[i].tl(), boundingBoxes[i].br(), TEXT_RECT_COLOR, 3);
        }

        if (MainActivity.TEST_MODE){
            writeSegment(img, "detecccion");
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
            //segments.add(ROI);

            if (MainActivity.TEST_MODE) {
                Log.d(TAG, "Writing segment.." + i);
                Log.d(TAG, "Size patch: "+filtered.cols()+"x"+filtered.rows());
                writeSegment(filtered, "" + i);
            }

            //OCR
            Bitmap bmp = getMatBitmap(filtered);
            OCR ocr = new OCR(context);
            ocr.setLanguage(lang_read);
            text = text + ocr.recognizeText(bmp) + " ";
            Log.d(TAG, "Text: "+text);
            //progressBar.setVisibility(View.INVISIBLE);
        }

        return text;
    }

    Mat equalize(Mat patch){
        //Equalize croped image
        Mat grayResult = new Mat();
        Imgproc.cvtColor(patch, grayResult, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayResult, grayResult, new Size(3, 3));
        //grayResult=histeq(grayResult);
        Imgproc.equalizeHist(grayResult, grayResult);
        return grayResult;
    }

    ArrayList<Mat> segment(List<Rect> boundingBoxes){
        //sort(boundingBoxes.begin(), boundingBoxes.end(), DetectText::spaticalOrder);
        ArrayList<Mat> segments = new ArrayList<>();
        for (int i = 0; i < boundingBoxes.size(); i++) {
            Rect box = boundingBoxes.get(i);
            Mat ROI = src.submat(box.y, box.y + box.height, box.x, box.x + box.width);
            Mat result = equalize(ROI);
            Mat scaled = resizeImage(result, 320, 60);
            segments.add(scaled);
        }

        return segments;
    }
}
