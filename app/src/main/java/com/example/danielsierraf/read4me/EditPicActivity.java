package com.example.danielsierraf.read4me;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.ProgressBar;


public class EditPicActivity extends ActionBarActivity {

    public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI = "com.example.danielsierraf.EditPicActivity.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.example.danielsierraf.EditPicActivity.PHOTO_DATA_PATH";
    public static final String EXTRA_ACTION = "com.example.danielsierraf.EditPicActivity.EXTRA_ACTION";

    private final int PIC_EDIT = 1;
    private static final String TAG = "EditPicActivity";

    private Uri mUri;
    private String mDataPath;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pic);

        final Intent intent = getIntent();
        //imageView = new ImageView(this);
        imageView = (ImageView) findViewById(R.id.img_to_edit);
        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        int action = intent.getIntExtra(EXTRA_ACTION, 1);

        Log.d(TAG, "ACTION: "+action);
        //Log.d(TAG, "mUri "+ mUri);
        if (action == 1) {
            mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
            imageView.setImageURI(mUri);
        } else {
            mUri = null;
            setPic();
        }


        //setContentView(imageView);

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
            case R.id.menu_edit:
                editPhoto();
                return true;

            default: return

            super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putParcelable("uri", mUri);
        savedInstanceState.putString("photo_path", mDataPath);
        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //setContentView(R.layout.activity_edit_pic);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        mUri = savedInstanceState.getParcelable("uri");
        mDataPath = savedInstanceState.getString("photo_path");
        imageView.setImageURI(mUri);
        //setContentView(imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "BEFORE IF DE DATA");
        if (resultCode == RESULT_OK) {
            if (requestCode == PIC_EDIT) {
                if (data != null) {
                    //setContentView(R.layout.activity_edit_pic);
                    mUri = (Uri) data.getData();
                    mDataPath = new ImageHandler(getApplicationContext()).getImagePath(mUri);
                    imageView.setImageURI(mUri);
                    //setContentView(imageView);
                }
            }
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
        Log.d(TAG, "Starting Edit Photo Activity");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.photo_edit_chooser_title)), PIC_EDIT);
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

    public void readText(View v){
        Context context = getApplicationContext();

        String lang_read = FileHandler.getDefaults(getString(R.string.lang_read), context);
        //String lang_hear = FileHandler.getDefaults(getString(R.string.lang_hear), context);
        Log.d(TAG, "Reading in "+lang_read);
        //Log.d(TAG, "Hearing "+lang_hear);
        //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
        //progressBar.setVisibility(View.VISIBLE);

        ImageProcessing imageProcessing = new ImageProcessing(context);
        imageProcessing.setMatGray(mDataPath);
        //image = imageProcessing.resizeImage(3000, 3000);
        //imageProcessing.resizeImage(2550, 2550);
        imageProcessing.otsuThreshold();
        Bitmap bmp = imageProcessing.getMatBitmap();
        imageProcessing.writeImage();

        //Bitmap bmp = new ImageHandler(context).getBitmapFromUri(mUri);
        Log.d(TAG, "Bitmap: "+bmp);

        OCR ocr = new OCR(context);
        ocr.setLanguage(lang_read);
        String text = ocr.recognizeText(bmp);
        Log.d(TAG, "Text: "+text);
        //progressBar.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, TTSActivity.class);
        intent.putExtra(TTSActivity.EXTRA_TEXT, text);
        startActivity(intent);
    }

    private void setPic() {
        try {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();

		/* Get the size of the image */
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mDataPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
            int scaleFactor = 1;
            if ((targetW > 0) || (targetH > 0)) {
                scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            }

		/* Set bitmap options to scale the image decode target */
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
            Bitmap bitmap = BitmapFactory.decodeFile(mDataPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
            imageView.setImageBitmap(bitmap);
        } catch (Exception e){
            Log.e(TAG, "Error seteando la foto");
            e.printStackTrace();
        }
        //imageView.setVisibility(View.VISIBLE);
        //mVideoView.setVisibility(View.INVISIBLE);
    }

}
