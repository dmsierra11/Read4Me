package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.example.danielsierraf.read4me.adapters.FullScreenImageAdapter;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.utils.Utils;
import com.example.danielsierraf.read4me.fragments.OCRFragment;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import java.util.Locale;


public class EditPicActivity extends Activity implements TextToSpeech.OnInitListener,
        ImageProcessingInterface {

    //public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI = "com.example.danielsierraf.EditPicActivity.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.example.danielsierraf.EditPicActivity.PHOTO_DATA_PATH";
    public static final String EXTRA_ACTION = "com.example.danielsierraf.EditPicActivity.EXTRA_ACTION";
    private static final int MY_DATA_CHECK_CODE = 1234;

    //private final int PIC_EDIT = 1;
    private static final String TAG = "EditPicActivity";

    private FragmentManager mFragmentManager;
    private AssetManager am;
    private Context mContext;
    private ImageProcessing imageProcessing;
    private DetectTextNative detectText;
    private OCRFragment mOCRFragment;
    private TextToSpeech mTts;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        am = getAssets();
        mContext = getApplicationContext();

        imageProcessing = new ImageProcessing(mContext);
        detectText = new DetectTextNative(am);

        setContentView(R.layout.activity_fullscreen_view);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        Utils utils = new Utils(getApplicationContext());

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);
        Log.d(TAG, "Position clicked: "+position);

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(EditPicActivity.this,
                utils.getFilePaths());

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_pic, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_delete:
                //deletePhoto();
                return true;
            case R.id.menu_edit:
                //editPhoto();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        //savedInstanceState.putParcelable("uri", mUri);
        //savedInstanceState.putString("photo_path", mDataPath);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //setContentView(R.layout.activity_edit_pic);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        //mUri = savedInstanceState.getParcelable("uri");
        //mDataPath = savedInstanceState.getString("photo_path");
        //imageView.setImageURI(mUri);
        //setContentView(imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            if (requestCode == MY_DATA_CHECK_CODE) {
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

    @Override
    public ImageProcessing getImageProcObject() {
        return imageProcessing;
    }

    @Override
    public DetectTextNative getDetectTextObject() {
        return detectText;
    }

    @Override
    public void notifyDetectionFinished(ImageProcessing imageProcessing, DetectTextNative textDetector) {
        setContentView(R.layout.main);

        this.imageProcessing = imageProcessing;
        this.detectText = textDetector;

        if (mOCRFragment == null)
            mOCRFragment = new OCRFragment();

        mFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mOCRFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mFragmentManager.executePendingTransactions();
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
        //Context context = getApplicationContext();
        String lang_hear = FileHandler.getDefaults(getString(R.string.lang_hear), mContext);
        String country_hear = FileHandler.getDefaults(getString(R.string.country_hear), mContext);
        Log.d(TAG, "Hearing " + lang_hear + ", " + country_hear);
        Locale loc = new Locale(lang_hear, country_hear);
        mTts.setLanguage(loc);
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void checkTTSResource() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }
}