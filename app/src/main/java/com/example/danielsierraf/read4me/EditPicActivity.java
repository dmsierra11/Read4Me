package com.example.danielsierraf.read4me;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.Toast;


public class EditPicActivity extends ActionBarActivity {

    public static final String PHOTO_MIME_TYPE = "image/ png";
    public static final String EXTRA_PHOTO_URI = "com.nummist.secondsight.LabActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.nummist.secondsight.LabActivity.extra.PHOTO_DATA_PATH";

    private Uri mUri;
    private String mDataPath;
    private final int PIC_CROP = 1;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_edit_pic);
        final Intent intent = getIntent();
        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        imageView = new ImageView(this);
        //ImageView imageView = (ImageView) findViewById(R.id.imageEdit);
        imageView.setImageURI(mUri);
        setContentView(imageView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_pic, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_delete:
                deletePhoto();
                return true;
            case R.id.menu_crop:
                performCrop();
                return true;
            case R.id.menu_edit:
                editPhoto();
                return true;

            default: return

            super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");

                imageView.setImageBitmap(selectedBitmap);
            }
        }

    }

    private void performCrop(){
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(mUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException e) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /* * Show a confirmation dialog. On confirmation, the photo is * deleted and the activity finishes. */

    private void deletePhoto() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditPicActivity.this);
        alert.setTitle( R.string.photo_delete_prompt_title);
        alert.setMessage( R.string.photo_delete_prompt_message);
        alert.setCancelable( false);

        alert.setPositiveButton( R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog, final int which) {
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.MediaColumns.DATA + "=?", new String[]{mDataPath});
                finish();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

    /* * Show a chooser so that the user may pick an app for editing the photo. */
    private void editPhoto() {
        final Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(mUri, PHOTO_MIME_TYPE);
        startActivity(Intent.createChooser(intent, getString(R.string.photo_edit_chooser_title)));
    }

    /* * Show a chooser so that the user may pick an app for sending * the photo. */
    private void sharePhoto() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PHOTO_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.photo_send_extra_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.photo_send_extra_text));
        startActivity( Intent.createChooser(intent, getString(R.string.photo_send_chooser_title)));
    }
}
