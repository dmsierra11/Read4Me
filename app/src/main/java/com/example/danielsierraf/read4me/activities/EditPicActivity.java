package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.utils.AppConstant;
import com.example.danielsierraf.read4me.utils.FileHandler;
import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.fragments.EditPicFragment;
import com.example.danielsierraf.read4me.fragments.OCRFragment;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;
import com.example.danielsierraf.read4me.utils.CustomUtils;
import com.example.danielsierraf.read4me.utils.Read4MeApp;

import java.util.Locale;


public class EditPicActivity extends Activity implements TextToSpeech.OnInitListener,
        ImageProcessingInterface, EditPicFragment.PicEditor {

    //public static final String PHOTO_MIME_TYPE = "image/png";
    //public static final String EXTRA_PHOTO_URI = "com.example.danielsierraf.EditPicActivity.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.example.danielsierraf.EditPicActivity.PHOTO_DATA_PATH";
    //public static final String EXTRA_ACTION = "com.example.danielsierraf.EditPicActivity.EXTRA_ACTION";
    //public static final String TAG_IMAGE_PROC_FRAGMENT = "imageProcFragment";
    private static final int MY_DATA_CHECK_CODE = 1234;

    //private final int PIC_EDIT = 1;
    private static final String TAG = "EditPicActivity";

//    private Uri mUri;
    private String mDataPath;
    private FragmentManager mFragmentManager;
    private AssetManager am;
    private Context mContext;
    private ImageProcessing imageProcessing;
    private DetectTextNative detectText;
    private OCRFragment mOCRFragment;
    private EditPicFragment mEditPicFragment;
    private TextToSpeech mTts;
    private String message;
    private Bitmap picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        am = getAssets();
        mContext = Read4MeApp.getInstance();

        final Intent intent = getIntent();
        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        picture = new CustomUtils(mContext).createBitmapFromPath(mDataPath);

        imageProcessing = new ImageProcessing(mDataPath);
        detectText = new DetectTextNative(am);

        if (mEditPicFragment == null)
            mEditPicFragment = new EditPicFragment();

        mFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mEditPicFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mFragmentManager.executePendingTransactions();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        //savedInstanceState.putParcelable("uri", mUri);
        savedInstanceState.putString("photo_path", mDataPath);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        mEditPicFragment = null;
        mTts.shutdown();
        super.onDestroy();
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //setContentView(R.layout.fragment_crop_image);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        //mUri = savedInstanceState.getParcelable("uri");
        mDataPath = savedInstanceState.getString("photo_path");
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
        this.detectText = textDetector;
        this.imageProcessing = imageProcessing;

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
    public Bitmap getPicture() {
        return picture;
    }

    @Override
    public void onInit(int status) {
        //mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "");
        //Context context = getApplicationContext();
        String lang_hear = FileHandler.getDefaults(getString(R.string.lang_hear), mContext);
        String country_hear = FileHandler.getDefaults(getString(R.string.country_hear), mContext);
        Log.d(TAG, "Hearing " + lang_hear + ", " + country_hear);
        Locale loc = new Locale (lang_hear, country_hear);
        mTts.setLanguage(loc);
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_langs) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkTTSResource(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }
}