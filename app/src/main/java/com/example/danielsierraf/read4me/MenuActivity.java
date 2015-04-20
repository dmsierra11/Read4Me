package com.example.danielsierraf.read4me;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;


public class MenuActivity extends ActionBarActivity {

    private final String TAG = "MenuActivity";
    private final int SELECT_PICTURE = 1;

    private String lang_read;
    private String lang_hear;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        lang_read = intent.getStringExtra(MainActivity.EXTRA_LANG_READ);
        lang_hear = intent.getStringExtra(MainActivity.EXTRA_LANG_HEAR);
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

    public void callRealTime(View v){
        //DO SOMETHING
        Toast.makeText(this, "Called real time", Toast.LENGTH_LONG).show();
    }

    public void callLoadPicture(View v){
        //DO SOMETHING
        //Toast.makeText(this, "Called load picture", Toast.LENGTH_LONG).show();
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_pic)),
                SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                Uri selectedImageUri = (Uri) data.getData();
                selectedImagePath = new ImageHandler(getApplicationContext()).getImagePath(selectedImageUri);

                Bitmap selectedBitmap = null;
                //selectedBitmap = (Bitmap) data.getParcelableExtra("data");
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (Exception e){
                    Log.e(TAG, "Image not found");
                    e.printStackTrace();
                }


                /* OPENCV
                Mat img = Highgui.imread(selectedImagePath, 0);
                //Mat grey = new Mat();

                //fixPhoto(img.getNativeObjAddr(), grey.getNativeObjAddr());
                //filterImage(img.getNativeObjAddr(), grey.getNativeObjAddr());
                */


                //Open the photo in anoter Activity.
                Intent intent = new Intent(this, EditPicActivity.class);
                intent.putExtra(EditPicActivity.EXTRA_PHOTO_URI, selectedImageUri);
                intent.putExtra(EditPicActivity.EXTRA_PHOTO_DATA_PATH, selectedImagePath);
                startActivity(intent);
            }
        }
    }

}
