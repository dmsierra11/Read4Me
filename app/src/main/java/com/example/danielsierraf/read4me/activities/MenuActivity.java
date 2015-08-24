package com.example.danielsierraf.read4me.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.danielsierraf.read4me.utils.ImageHandler;
import com.example.danielsierraf.read4me.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MenuActivity extends Activity {

    public static final int TAKE_PHOTO_ACTION = 1;
    public static final int REAL_TIME_ACTION = 2;

    private final String TAG = "MenuActivity";
    private final int SELECT_PICTURE = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "RESULT CODE: "+resultCode);
        switch (requestCode){
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Picture taken");

                    //handleBigCameraPhoto();
                    galleryAddPic();

                    //Open the photo in anoter Activity.
                    Intent intent = new Intent(this, EditPicActivity.class);
                    intent.putExtra(EditPicActivity.EXTRA_PHOTO_DATA_PATH, mCurrentPhotoPath);
                    intent.putExtra(EditPicActivity.EXTRA_ACTION, REQUEST_IMAGE_CAPTURE);
                    startActivity(intent);
                    //handleBigCameraPhoto();
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Picture selected");

                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = new ImageHandler(getApplicationContext()).getImagePath(selectedImageUri);

                    //Open the photo in anoter Activity.
                    Intent intent = new Intent(this, EditPicActivity.class);
                    intent.putExtra(EditPicActivity.EXTRA_PHOTO_URI, selectedImageUri);
                    intent.putExtra(EditPicActivity.EXTRA_PHOTO_DATA_PATH, selectedImagePath);
                    intent.putExtra(EditPicActivity.EXTRA_ACTION, SELECT_PICTURE);
                    startActivity(intent);
                }
                break;


        }
    }

    public void callRealTime(View v){
        //DO SOMETHING
        //Toast.makeText(this, "Called real time", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TextDetectionActivity.class);
        intent.putExtra("action", REAL_TIME_ACTION);
        startActivity(intent);
    }

    public void callLoadPicture(View v){
        //DO SOMETHING
        //Toast.makeText(this, "Called load picture", Toast.LENGTH_LONG).show();
        // select a file
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_pic)),
                SELECT_PICTURE);*/
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivity(intent);

    }

    public void callTakePicture(View v){
        // create Intent to take a picture and return control to the calling application
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = createImageFile();
            mCurrentPhotoPath = f.getPath();
            Log.d(TAG, "PATH: "+mCurrentPhotoPath);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);*/
        Intent intent = new Intent(this, TextDetectionActivity.class);
        intent.putExtra("action", TAKE_PHOTO_ACTION);
        startActivity(intent);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
