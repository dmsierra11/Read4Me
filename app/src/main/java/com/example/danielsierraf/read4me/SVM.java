package com.example.danielsierraf.read4me;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;
import org.opencv.utils.Converters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielsierraf on 8/3/15.
 */
public class SVM {

    private static final String TAG = "SVM";
    protected static final String PATH_POSITIVE = MainActivity.appFolder+"/patchesForTraining/1";
    protected static final String PATH_NEGATIVE = MainActivity.appFolder+"/patchesForTraining/0";
    protected static final String XML = MainActivity.appFolder+"/SVM.xml";
    //protected static final String FILE_TEST = "data/negativo/1.jpg";

    private static Mat trainingImages;
    private static Mat trainingLabels;
    private static Mat trainingData;
    private static Mat classes;

    public static CvSVM clasificador;

    static {
        //System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        System.loadLibrary("opencv_java");
        trainingImages = new Mat();
        trainingLabels = new Mat();
        trainingData = new Mat();
        classes = new Mat();
    }

    public SVM() {
        Log.d(TAG, "Training positives");
        trainPositive();
        Log.d(TAG, "Training negatives");
        trainNegative();
        Log.d(TAG, "Saving training");
        train();
        //test();
    }

    protected static float applySVM(Mat in) {
        //Mat in = Highgui.imread( new File( file ).getAbsolutePath(), Highgui.CV_LOAD_IMAGE_GRAYSCALE );
        clasificador.load( new File( XML ).getAbsolutePath() );
        System.out.println( clasificador );
        Mat out = new Mat();
        in.convertTo( out, CvType.CV_32FC1 );
        out = out.reshape( 1, 1 );
        System.out.println( out );
        float response = clasificador.predict( out );
        System.out.println( clasificador.predict( out ) );
        return response;
    }

    protected static void train() {
        trainingImages.copyTo( trainingData );
        trainingData.convertTo( trainingData, CvType.CV_32FC1 );
        trainingLabels.copyTo( classes );

        //params
        //CvSVMParams params = new CvSVMParams();
        //params.set_kernel_type( CvSVM.LINEAR );

        CvSVMParams SVM_params = new CvSVMParams();
        //SVM_params.svm_type = CvSVM::C_SVC;
        SVM_params.set_svm_type(CvSVM.C_SVC);
        //SVM_params.kernel_type = CvSVM::LINEAR; //CvSVM::LINEAR;
        SVM_params.set_kernel_type(CvSVM.LINEAR);
        //SVM_params.degree = 0;
        SVM_params.set_degree(0);
        //SVM_params.gamma = 1;
        SVM_params.set_gamma(1);
        //SVM_params.coef0 = 0;
        SVM_params.set_coef0(0);
        //SVM_params.C = 1;
        SVM_params.set_C(1);
        //SVM_params.nu = 0;
        SVM_params.set_nu(0);
        //SVM_params.p = 0;
        SVM_params.set_p(0);
        //SVM_params.term_crit = cvTermCriteria(CV_TERMCRIT_ITER, 1000, 0.01);
        TermCriteria termCriteria = new TermCriteria(TermCriteria.MAX_ITER,1000, 0.01);
        SVM_params.set_term_crit(termCriteria);

        clasificador = new CvSVM( trainingData, classes, new Mat(), new Mat(), SVM_params );
        clasificador.save( XML );
    }

    protected static void trainPositive() {
        for ( File file : new File( PATH_POSITIVE ).listFiles() ) {
            Mat img = getMat( file.getAbsolutePath() );
            trainingImages.push_back( img.reshape( 1, 1 ) );
            trainingLabels.push_back( Mat.ones( new Size( 1, 1 ), CvType.CV_32FC1 ) );
        }
    }

    protected static void trainNegative() {
        for ( File file : new File( PATH_NEGATIVE ).listFiles() ) {
            Mat img = getMat( file.getAbsolutePath() );
            trainingImages.push_back( img.reshape( 1, 1 ) );
            trainingLabels.push_back( Mat.zeros( new Size( 1, 1 ), CvType.CV_32FC1 ) );
        }
    }

    protected static Mat getMat( String path ) {
        Mat img = new Mat();
        Mat con = Highgui.imread( path, Highgui.CV_LOAD_IMAGE_GRAYSCALE );
        con.convertTo( img, CvType.CV_32FC1, 1.0 / 255.0 );
        return img;
    }


    public Rect[] getWordsBoxes(ArrayList<Mat> segments, List<Rect> boundingBoxes_){
        ArrayList<Rect> boxesBothSides_ = new ArrayList<>();
        List<Rect> posible_regions = boundingBoxes_;
        for(int i=0; i< segments.size(); i++)
        {
            Mat img = segments.get(i);
            float response = applySVM(img);
            Log.d(TAG, "Response= "+response);
            if(response==1.0)
                boxesBothSides_.add(posible_regions.get(i));
        }
        Rect[] boxes = new Rect[boxesBothSides_.size()];
        boxesBothSides_.toArray(boxes);
        return boxes;
    }


    /*public Rect[] applySVM(ArrayList<Mat> segments, List<Rect> boundingBoxes_){
        ArrayList<Rect> boxesBothSides_ = new ArrayList<>();

        //Set SVM params
        CvSVMParams SVM_params = new CvSVMParams();
        //SVM_params.svm_type = CvSVM::C_SVC;
        SVM_params.set_svm_type(CvSVM.C_SVC);
        //SVM_params.kernel_type = CvSVM::LINEAR; //CvSVM::LINEAR;
        SVM_params.set_kernel_type(CvSVM.LINEAR);
        //SVM_params.degree = 0;
        SVM_params.set_degree(0);
        //SVM_params.gamma = 1;
        SVM_params.set_gamma(1);
        //SVM_params.coef0 = 0;
        SVM_params.set_coef0(0);
        //SVM_params.C = 1;
        SVM_params.set_C(1);
        //SVM_params.nu = 0;
        SVM_params.set_nu(0);
        //SVM_params.p = 0;
        SVM_params.set_p(0);
        //SVM_params.term_crit = cvTermCriteria(CV_TERMCRIT_ITER, 1000, 0.01);
        TermCriteria termCriteria = new TermCriteria(TermCriteria.MAX_ITER,1000, 0.01);
        SVM_params.set_term_crit(termCriteria);
        //Train SVM
        //CvSVM svmClassifier(SVM_TrainingData, SVM_Classes, Mat(), Mat(), SVM_params);
        CvSVM svmClassifier = new CvSVM(this.trainingData, this.classes, new Mat(), new Mat(), SVM_params);
        //CvSVM svmClassifier = new CvSVM(null, null, new Mat(), new Mat(), SVM_params);
        //CvSVM svmClassifier = new CvSVM();
        Log.d(TAG, "Loading svm file..");
        svmClassifier.load(MainActivity.appFolder+"/SVM.xml");
        Log.d(TAG, "Loaded svm file..");

        //Classify words or no words
        List<Rect> posible_regions = boundingBoxes_;
        //sort(posible_regions.begin(), posible_regions.end(), DetectText::spaticalOrder);
        for(int i=0; i< segments.size(); i++)
        {
            //Mat img=segments[i];
            Mat img = segments.get(i);
            Mat p= img.reshape(1, 1);
            p.convertTo(p, CvType.CV_32FC1);

            int response = (int)svmClassifier.predict( p );
            //cout << "Response: " << response << endl;
            Log.d(TAG, "Response= "+response);
            if(response==1)
                boxesBothSides_.add(posible_regions.get(i));
        }
        Rect[] boxes = new Rect[boxesBothSides_.size()];
        boxesBothSides_.toArray(boxes);
        return boxes;
    }

    public void train(){

        String path_Words = MainActivity.appFolder+"/patchesForTraining/1";
        Log.d(TAG, "Path words: "+path_Words);
        String path_NoWords = MainActivity.appFolder+"/patchesForTraining/0";
        Log.d(TAG, "Path no words: "+path_NoWords);

        Mat classes = new Mat();
        Mat trainingData = new Mat();
        Mat trainingImages = new Mat();
        Mat trainingLabels = new Mat();

        for (File file : new File(path_Words).listFiles()) {
            Mat img = new Mat();
            Log.d(TAG, "Reading "+file.getPath());
            Mat m = Highgui.imread(file.getPath(), 1);
            Imgproc.cvtColor(m, img, Imgproc.COLOR_BGR2GRAY);

            img= img.reshape(1, 1);
            trainingImages.push_back(img);
            //trainingLabels.add(1);
            trainingLabels.push_back( Mat.ones( new Size( 1, 1 ), CvType.CV_32FC1 ) );
        }

        for (File file : new File(path_NoWords).listFiles()) {
            Mat img = new Mat();
            Mat m = Highgui.imread(file.getPath(), 1);
            Imgproc.cvtColor(m, img, Imgproc.COLOR_BGR2GRAY);

            img= img.reshape(1, 1);
            trainingImages.push_back(img);
            //trainingLabels.add(0);
            trainingLabels.push_back( Mat.zeros( new Size( 1, 1 ), CvType.CV_32FC1 ) );
        }

        trainingImages.copyTo(trainingData);
        trainingData.convertTo(trainingData, CvType.CV_32FC1);
        trainingLabels.copyTo(classes);

        CvSVM svmClassifier = new CvSVM(this.trainingData, this.classes);
        Log.d(TAG, "SVM.xml saving");
        svmClassifier.save(MainActivity.appFolder+"/SVM.xml");
        Log.d(TAG, "SVM.xml saved");
    }*/
}
