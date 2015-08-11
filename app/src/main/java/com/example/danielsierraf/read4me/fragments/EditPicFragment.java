package com.example.danielsierraf.read4me.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.danielsierraf.read4me.R;
import com.example.danielsierraf.read4me.activities.EditPicActivity;
import com.example.danielsierraf.read4me.classes.DetectTextNative;
import com.example.danielsierraf.read4me.classes.ImageHandler;
import com.example.danielsierraf.read4me.classes.ImageProcessing;
import com.example.danielsierraf.read4me.interfaces.ImageProcessingInterface;

import org.opencv.core.Mat;

/**
 * Created by danielsierraf on 8/10/15.
 */
public class EditPicFragment extends Fragment {

    public static final String PHOTO_MIME_TYPE = "image/png";

    private static final String TAG = "EditPicFragment";
    private final int PIC_EDIT = 1;

    private ImageView imageView;
    private Button btnRead;
    private ProgressBar progressBar;
    private Uri mUri;
    private String mDataPath;
    private Context mContext;
    //private int RESULT_OK;
    private Activity mActivity;
    private ImageProcessingInterface mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
        mActivity = activity;
        //RESULT_OK = activity.RESULT_OK;
        try {
            mCallback = (ImageProcessingInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ImageProcessingInterface");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args_ =  getArguments();
        int action = args_.getInt(EditPicActivity.EXTRA_ACTION);
        mDataPath = args_.getString(EditPicActivity.EXTRA_PHOTO_DATA_PATH);

        mUri = (action == 1) ? (Uri) args_.getParcelable(EditPicActivity.EXTRA_PHOTO_URI) : null;

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit_pic, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imageView = (ImageView) getActivity().findViewById(R.id.img_to_edit);
        if (mUri != null)
            imageView.setImageURI(mUri);
        else setPic();

        btnRead = (Button) getActivity().findViewById(R.id.btn_read_text);
        btnRead.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           readText();
                                       }
                                   }

        );

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBarImgProc);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mActivity = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options Menu using quote_menu.xml
        Log.d(TAG, "OnCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_pic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId())
        {
            case R.id.menu_delete:
                deletePhoto();
                return true;
            case R.id.menu_edit:
                editPhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "On Activity Result");
        if (resultCode == mActivity.RESULT_OK) {
            if (requestCode == PIC_EDIT) {
                if (data != null) {
                    //setContentView(R.layout.activity_edit_pic);
                    mUri = data.getData();
                    mDataPath = new ImageHandler(mContext).getImagePath(mUri);
                    imageView.setImageURI(mUri);
                    //setContentView(imageView);
                }
            }
        }
    }

    /* * Show a confirmation dialog. On confirmation, the photo is * deleted and the activity finishes. */

    private void deletePhoto() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle( R.string.photo_delete_prompt_title);
        alert.setMessage( R.string.photo_delete_prompt_message);
        alert.setCancelable( false);

        alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                mContext.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.MediaColumns.DATA + "=?", new String[]{mDataPath});
                mActivity.finish();
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
        startActivity(Intent.createChooser(intent, getString(R.string.photo_send_chooser_title)));
    }

    public void readText(){
        ImageProcessingTask imageProcessingTask = new ImageProcessingTask();
        imageProcessingTask.execute();
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

    public class ImageProcessingTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Deteccion de texto (nativo c++)
            DetectTextNative detectText = mCallback.getDetectTextObject();
            ImageProcessing imageProcessing = mCallback.getImageProcObject();
            Mat src = imageProcessing.getMat();
            detectText.detect(src.getNativeObjAddr());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Detection finished");
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            mCallback.notifyDetectionFinished();
        }
    }
}
