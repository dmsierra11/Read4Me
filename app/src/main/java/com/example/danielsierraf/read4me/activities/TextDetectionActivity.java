package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.FileHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.fragments.OCRFragment;
import com.example.danielsierraf.read4me.fragments.TextDetectionFragment;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import java.util.Locale;

public class TextDetectionActivity extends Activity implements ImageProcessingInterface,
        TextToSpeech.OnInitListener{
    private static final String  TAG = "ColorDetectionActivity";
    private static final int MY_DATA_CHECK_CODE = 1234;

    private AssetManager am;
    private Context mContext;
    private TextDetectionFragment mTextDetectionFragment;
    private OCRFragment mOCRFragment;
    private ImageProcessing imageProcessing;
    private DetectTextNative textDetector;
    private TextToSpeech mTts;
    private String message;
    private FragmentManager mFragmentManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mContext = getApplicationContext();
        am = getAssets();

        imageProcessing = new ImageProcessing(mContext);
        textDetector = new DetectTextNative(am);

        Intent intent = getIntent();
        int action = intent.getIntExtra("action", 0);

        if (mTextDetectionFragment == null)
            mTextDetectionFragment = new TextDetectionFragment();

        Bundle args_ = new Bundle();
        args_.putInt("action", action);
        mTextDetectionFragment.setArguments(args_);

        mFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mTextDetectionFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mFragmentManager.executePendingTransactions();
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            textDetector.finalize();
        } catch (Throwable throwable) {
            Log.e(TAG, "Error finalizando");
            throwable.printStackTrace();
        }
        Log.d(TAG, "Deteccion finalizada");
    }

    @Override
    public void onDestroy()
    {
        mTextDetectionFragment = null;
        // Don't forget to shutdown!
        if (mTts != null)
        {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public ImageProcessing getImageProcObject() {
        return imageProcessing;
    }

    @Override
    public DetectTextNative getDetectTextObject() {
        return textDetector;
    }

    @Override
    public void notifyDetectionFinished(ImageProcessing imageProcessing, DetectTextNative textDetector) {
        this.textDetector = textDetector;
        this.imageProcessing = imageProcessing;
        if (mOCRFragment == null)
            mOCRFragment = new OCRFragment();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mOCRFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void notifyOCRFinished(String text) {
        Log.d(TAG, "Recognized text: " + text);
        message = text;
        checkTTSResource();
    }

    @Override
    public void onInit(int status) {
        //mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "");
        Context context = getApplicationContext();
        String lang_hear = FileHandler.getDefaults(getString(R.string.lang_hear), context);
        String country_hear = FileHandler.getDefaults(getString(R.string.country_hear), context);
        Log.d(TAG, "Hearing " + lang_hear + ", " + country_hear);
        Locale loc = new Locale (lang_hear, country_hear);
        mTts.setLanguage(loc);
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void checkTTSResource(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
}
